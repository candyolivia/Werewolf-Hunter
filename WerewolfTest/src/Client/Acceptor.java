/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Client;

import Server.ListPlayer;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 *
 * @author User
 */
public class Acceptor {
    DatagramSocket socket;
    byte[] buf;
    DatagramPacket sendData;
    DatagramPacket receiveData;
    private ListPlayer listPlayers;
    private boolean isProposer;
    private boolean isLeader = false;
    private int playerID;
    private Proposer proposer;
    
    public void getListClient() throws JSONException { //Masih dummy
        JSONObject listClient = new JSONObject();
        JSONObject client1 = new JSONObject();
        client1.put("player_id",0);
        client1.put("is_alive",1);
        client1.put("address","127.0.0.1");
        client1.put("port", 1111);
        client1.put("username", "sister");
        client1.put("role", "civillian");
        
        JSONObject client2 = new JSONObject();
        client2.put("player_id",1);
        client2.put("is_alive",1);
        client2.put("address","127.0.0.1");
        client2.put("port", 2222);
        client2.put("username", "gaib");
        client2.put("role", "werewolf");
        
        JSONObject client3 = new JSONObject();
        client3.put("player_id",2);
        client3.put("is_alive",1);
        client3.put("address","127.0.0.1");
        client3.put("port", 3333);
        client3.put("username", "irk");
        client3.put("role", "werewolf");
                
        JSONObject client4 = new JSONObject();
        client4.put("player_id",3);
        client4.put("is_alive",1);
        client4.put("address","127.0.0.1");
        client4.put("port", 4444);
        client4.put("username", "basdat");
        client4.put("role", "civillian");
        
        JSONObject client5 = new JSONObject();
        client5.put("player_id",4);
        client5.put("is_alive",1);
        client5.put("address","127.0.0.1");
        client5.put("port", 5555);
        client5.put("username", "rpl");
        client5.put("role", "civillian");
        
        JSONObject client6 = new JSONObject();
        client6.put("player_id",5);
        client6.put("is_alive",1);
        client6.put("address","127.0.0.1");
        client6.put("port", 6666);
        client6.put("username", "labpro");
        client6.put("role", "civillian");


        listClient.put("status", "ok");
        listClient.put("clients", new JSONArray(new Object[] { client1, client2, client3, client4, client5, client6} ));
        listClient.put("description", "list of clients retrieved");
        System.out.println(listClient.toString());
        
        for (int i = 0; i < 6; i++) {
            int playerId = listClient.getJSONArray("clients").getJSONObject(i).getInt("player_id");
            String username = listClient.getJSONArray("clients").getJSONObject(i).getString("username");
            int role;
            if (listClient.getJSONArray("clients").getJSONObject(i).getString("role").equals("civillian")) {
                role = 0;
            } else {
                role = 1;
            }
            listPlayers.addPlayer(playerId, username, role);
        }
    }
    
    public Acceptor() throws SocketException, JSONException {
        getListClient();
        chooseKPU();
    }
    
    public void chooseKPU() throws SocketException, JSONException {
        int[] listPlayerID = new int[6];
        
        for (int i = 0; i < listPlayers.getSize(); i++) {
            listPlayerID[i] = listPlayers.getPlayer(i).getId();
        }
        
        Arrays.sort(listPlayerID);
        
        if (playerID == listPlayerID[listPlayerID.length-1] || playerID == listPlayerID[listPlayerID.length-2]) {
            isProposer = true;
        } else {
            isProposer = false;
        }
        
        if (isProposer) {
            proposer = new Proposer(socket);
            proposer.setListPlayers(listPlayers);
            proposer.setPlayerID(playerID);
            proposer.setSocket(socket);
            Thread proposerThread = new Thread(proposer);
            proposerThread.start();
        }
        
        byte[] buf = new byte[1024];
        receiveData = new DatagramPacket(buf, buf.length);
        int port = 1111;
        for (int i = 0; i < listPlayerID.length; i++) {
            if (playerID == listPlayers.getPlayer(i).getId()) {
                port = Integer.parseInt(listPlayers.getPlayer(i).getPort());
                break;
            }
        }
        
        socket = new DatagramSocket(port);
        receiveRequest();
        
        if (isProposer) {
            try {
                chooseLeader();
                if (isLeader) {
                    broadcastAcceptedProposal();
                }
            } catch (IOException ex) {
                Logger.getLogger(Acceptor.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
    
    public void receiveRequest() throws JSONException {
        boolean leaderIsSelected = false;
        while(!leaderIsSelected){
            try {                
                if (isProposer) { 
                    socket.receive(receiveData);
                    String otherProposer = new String(receiveData.getData(), 0, receiveData.getLength());
                    JSONObject otherJSON = new JSONObject(otherProposer);
                    JSONArray arr = otherJSON.getJSONArray("proposer_id");
                    int otherProposerProposalID = arr.getInt(0);
                    int otherProposerID = arr.getInt(1);
                    JSONObject OKToOtherProposer = new JSONObject();
                    OKToOtherProposer.put("status", "ok");
                    OKToOtherProposer.put("description", "accepted");
                    OKToOtherProposer.put("previous_accepted", "-999"); 
                    
                    buf = OKToOtherProposer.toString().getBytes();
                    InetAddress address = InetAddress.getByAddress(listPlayers.getPlayer(otherProposerID).getAddress().getBytes());
                    int port = Integer.parseInt(listPlayers.getPlayer(otherProposerID).getPort());
                    sendData = new DatagramPacket(buf, buf.length, address,port);
                    socket.send(sendData);
                    leaderIsSelected = true;
                } else { //Harus nunggu 2 proposal
                    socket.receive(receiveData);
                    String firstProposer = new String(receiveData.getData(), 0, receiveData.getLength());
                    JSONObject firstProposerJSON = new JSONObject(firstProposer);
                    JSONArray arr1 = firstProposerJSON.getJSONArray("proposer_id");
                    int firstProposerProposalID = arr1.getInt(0);
                    int firstProposerID = arr1.getInt(1);
                    
                    socket.receive(receiveData);
                    String secondProposer = new String(receiveData.getData(), 0, receiveData.getLength());
                    JSONObject secondProposerJSON = new JSONObject(secondProposer);
                    JSONArray arr2 = secondProposerJSON.getJSONArray("proposer_id");
                    int secondProposerProposalID = arr2.getInt(0);
                    int secondProposerID = arr2.getInt(1);
                    
                    JSONObject okResponse = new JSONObject();
                    okResponse.put("status", "ok");
                    okResponse.put("description", "accepted");
                    okResponse.put("previous_accepted", "-999"); 
                    
                    JSONObject failResponse = new JSONObject();
                    failResponse.put("status", "fail");
                    failResponse.put("description", "rejected");
                    
                    if ((firstProposerProposalID < secondProposerProposalID)||((firstProposerProposalID == secondProposerProposalID)
                            && (firstProposerID < secondProposerID))) {
                        buf = okResponse.toString().getBytes();
                        InetAddress address = InetAddress.getByAddress(listPlayers.getPlayer(secondProposerID).getAddress().getBytes());
                        int port = Integer.parseInt(listPlayers.getPlayer(secondProposerID).getPort());
                        sendData = new DatagramPacket(buf, buf.length, address,port);
                        socket.send(sendData);
                        
                        buf = failResponse.toString().getBytes();
                        address = InetAddress.getByAddress(listPlayers.getPlayer(firstProposerID).getAddress().getBytes());
                        port = Integer.parseInt(listPlayers.getPlayer(firstProposerID).getPort());
                        sendData = new DatagramPacket(buf, buf.length, address,port);
                        socket.send(sendData);
                        leaderIsSelected = true;
                    } else if ((firstProposerProposalID > secondProposerProposalID)||((firstProposerProposalID == secondProposerProposalID)
                            && (firstProposerID > secondProposerID))){
                        buf = okResponse.toString().getBytes();
                        InetAddress address = InetAddress.getByAddress(listPlayers.getPlayer(firstProposerID).getAddress().getBytes());
                        int port = Integer.parseInt(listPlayers.getPlayer(firstProposerID).getPort());
                        sendData = new DatagramPacket(buf, buf.length, address,port);
                        socket.send(sendData);
                        
                        buf = failResponse.toString().getBytes();
                        address = InetAddress.getByAddress(listPlayers.getPlayer(secondProposerID).getAddress().getBytes());
                        port = Integer.parseInt(listPlayers.getPlayer(secondProposerID).getPort());
                        sendData = new DatagramPacket(buf, buf.length, address,port);
                        socket.send(sendData);
                        leaderIsSelected = true;
                    } else {
                        proposer.prepareProposal();
                    }
                }
                
            } catch (SocketException ex) {
                Logger.getLogger(Receiver.class.getName()).log(Level.SEVERE, null, ex);
            } catch (IOException ex) {
                Logger.getLogger(Receiver.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
    
    public void chooseLeader() throws IOException, JSONException {
        int count = 0;
        String[] acceptorDecisions = new String[listPlayers.getSize()-1];
        for (int i = 0; i < listPlayers.getSize()-1; i++) {
            socket.receive(receiveData);
            acceptorDecisions[i] = new String(receiveData.getData(), 0, receiveData.getLength());
            JSONObject acceptorDecisionsJSON = new JSONObject(acceptorDecisions[i]);
            String status = acceptorDecisionsJSON.getString("status");
            if (status.equals("ok")) {
                count++;
            }
        }
        
        if (count > (listPlayers.getSize()/2 + 1)) {
            isLeader = true;
        }
    }
    
    public void sendAcceptedProposal(JSONObject request, int playerId) {
        try {
            byte[] buf = request.toString().getBytes();
            InetAddress address = InetAddress.getByAddress(listPlayers.getPlayer(playerId).getAddress().getBytes());
            int port = Integer.parseInt(listPlayers.getPlayer(playerId).getPort());
            sendData = new DatagramPacket(buf, buf.length, address,port);
            socket.send(sendData);
        } catch (IOException ex) {
            Logger.getLogger(Proposer.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void broadcastAcceptedProposal() throws JSONException {
        JSONObject acceptedProposalJSON = new JSONObject();
        acceptedProposalJSON.put("method","accept_proposal");
        acceptedProposalJSON.put("proposal_id", new JSONArray(new Object[]{proposer.getProposalID(), playerID}));
        acceptedProposalJSON.put("kpu_id", playerID);
        
        for (int i = 0; i < listPlayers.getSize(); i++) {
            sendAcceptedProposal(acceptedProposalJSON, i);
        }
    }
}

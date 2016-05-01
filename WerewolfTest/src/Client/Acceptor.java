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
import java.net.UnknownHostException;
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
public class Acceptor implements Runnable {
    DatagramSocket socketUDP;
    byte[] buf;
    DatagramPacket sendData;
    DatagramPacket receiveData;
    private ListPlayer listPlayers;
    private boolean isProposer = false;
    private boolean checkIsProposerTime = false;
    private boolean isKPU = false;
    private boolean isLeader = false;
    private boolean isLeaderSelected = false;
    private int playerID;
    private Proposer proposer;
    private boolean isConsensusTime = true;
    private boolean isSendProposalTime  = false;
    private int counterPromiseNotProposer = 0;
    private int firstProposerProposalID, secondProposerProposalID, firstProposerID, secondProposerID;
    private int previousKPUId = -999;
    private int countConsesusPaxos = 0;
    private int countReceivePromise = 0;
    private int countReceiveAccept = 0;
    
    public Acceptor(DatagramSocket _socketUDP) throws SocketException{
        this.socketUDP = _socketUDP;
        proposer = new Proposer(listPlayers, playerID);
    }
            
    @Override
    public void run() {
        
        while(true){
            if(isConsensusTime){
                if(checkIsProposerTime){
                    checkIsProposer();
                    checkIsProposerTime = false;
                    isSendProposalTime = true;
                }
                if(isSendProposalTime){
                    if(isProposer){
                        try {
                            proposer.prepareProposal();
                        } catch (JSONException ex) {
                            Logger.getLogger(Acceptor.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    }
                    countConsesusPaxos = 0;
                    countReceivePromise = 0;
                    isLeaderSelected = false;
                    isSendProposalTime = false;
                }
                if(!proposer.getIsSendRequestTime()){
                    buf = new byte[1024];
                    receiveData = new DatagramPacket(buf, buf.length);
                    try {
                        socketUDP.receive(receiveData);
                        String otherProposer = new String(receiveData.getData(), 0, receiveData.getLength());
                        JSONObject otherJSON = new JSONObject(otherProposer);
                        if(otherJSON.getString("method") == "prepare_proposal"){
                            promise(otherJSON);
                        }
                        if(otherJSON.getString("method") == "accept_proposal"){
                            accept(otherJSON);
                        }
                        if(isProposer){
                            if(otherJSON.getString("status") != null){
                                if(!isLeaderSelected){
                                    countReceivePromise++;
                                    if(otherJSON.getString("status") == "ok") countConsesusPaxos++;
                                    if(countReceivePromise == listPlayers.getSize()-1) {
                                        if(consensusPaxos(countConsesusPaxos)) isLeader = true;
                                        countConsesusPaxos = 0;
                                        if(isLeader){
                                            proposer.setIsSendRequestTime(true);
                                            broadcastAcceptedProposal();
                                            proposer.setIsSendRequestTime(false);
                                        }
                                        isLeaderSelected = true;
                                    }
                                }
                                if(isLeader){
                                    countReceiveAccept++;
                                    if(otherJSON.getString("status") == "ok") countConsesusPaxos++;
                                    if(countReceiveAccept == listPlayers.getSize()-1){
                                        if(consensusPaxos(countConsesusPaxos)){
                                            isConsensusTime = false;
                                            isKPU = true;
                                            isLeader = false;
                                        } else {
                                            proposer.setIsSendRequestTime(true);
                                            broadcastAcceptedProposal();
                                            proposer.setIsSendRequestTime(false);
                                        }
                                    }
                                }
                            }
                        }
                    } catch (IOException ex) {
                        Logger.getLogger(Acceptor.class.getName()).log(Level.SEVERE, null, ex);
                    } catch (JSONException ex) {
                        Logger.getLogger(Acceptor.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            } else if(isKPU){
            
            }
        }
    }
    
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
        client3.put("address","127.0.0.1"   );
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
            listPlayers.addPlayer(playerId, username);
        }
    }
    
    public void checkIsProposer(){
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
    }
            
    public void promise(JSONObject otherJSON) throws JSONException {
        try {                
            if (isProposer) {
                JSONArray arr = otherJSON.getJSONArray("proposer_id");
                int otherProposerProposalID = arr.getInt(0);
                int otherProposerID = arr.getInt(1);
                JSONObject OKToOtherProposer = new JSONObject();
                OKToOtherProposer.put("status", "ok");
                OKToOtherProposer.put("description", "accepted");
                if(previousKPUId != -999) OKToOtherProposer.put("previous_accepted", previousKPUId); 

                buf = OKToOtherProposer.toString().getBytes();
                InetAddress address = InetAddress.getByAddress(listPlayers.getPlayer(otherProposerID).getAddress().getBytes());
                int port = Integer.parseInt(listPlayers.getPlayer(otherProposerID).getPort());
                sendData = new DatagramPacket(buf, buf.length, address,port);
                socketUDP.send(sendData);
            } else { //Harus nunggu 2 proposal
                ++counterPromiseNotProposer;
                JSONArray arr = otherJSON.getJSONArray("proposer_id");
                if(counterPromiseNotProposer < 2){
                    firstProposerProposalID = arr.getInt(0);
                    firstProposerID = arr.getInt(1);
                } else {
                    secondProposerProposalID = arr.getInt(0);
                    secondProposerID = arr.getInt(1);
                    
                    JSONObject okResponse = new JSONObject();
                    okResponse.put("status", "ok");
                    okResponse.put("description", "accepted");
                    if(previousKPUId != -999) okResponse.put("previous_accepted", previousKPUId); 

                    JSONObject failResponse = new JSONObject();
                    failResponse.put("status", "fail");
                    failResponse.put("description", "rejected");

                    if ((firstProposerProposalID < secondProposerProposalID)||((firstProposerProposalID == secondProposerProposalID)
                            && (firstProposerID < secondProposerID))) {
                        buf = okResponse.toString().getBytes();
                        InetAddress address = InetAddress.getByAddress(listPlayers.getPlayer(secondProposerID).getAddress().getBytes());
                        int port = Integer.parseInt(listPlayers.getPlayer(secondProposerID).getPort());
                        sendData = new DatagramPacket(buf, buf.length, address,port);
                        socketUDP.send(sendData);

                        buf = failResponse.toString().getBytes();
                        address = InetAddress.getByAddress(listPlayers.getPlayer(firstProposerID).getAddress().getBytes());
                        port = Integer.parseInt(listPlayers.getPlayer(firstProposerID).getPort());
                        sendData = new DatagramPacket(buf, buf.length, address,port);
                        socketUDP.send(sendData);
                    } else if ((firstProposerProposalID > secondProposerProposalID)||((firstProposerProposalID == secondProposerProposalID)
                            && (firstProposerID > secondProposerID))){
                        buf = okResponse.toString().getBytes();
                        InetAddress address = InetAddress.getByAddress(listPlayers.getPlayer(firstProposerID).getAddress().getBytes());
                        int port = Integer.parseInt(listPlayers.getPlayer(firstProposerID).getPort());
                        sendData = new DatagramPacket(buf, buf.length, address,port);
                        socketUDP.send(sendData);

                        buf = failResponse.toString().getBytes();
                        address = InetAddress.getByAddress(listPlayers.getPlayer(secondProposerID).getAddress().getBytes());
                        port = Integer.parseInt(listPlayers.getPlayer(secondProposerID).getPort());
                        sendData = new DatagramPacket(buf, buf.length, address,port);
                        socketUDP.send(sendData);
                    } else {
                        isSendProposalTime = true;
                    }
                    counterPromiseNotProposer = 0;
                }
            }   
            } catch (SocketException ex) {
                Logger.getLogger(Receiver.class.getName()).log(Level.SEVERE, null, ex);
            } catch (IOException ex) {
                Logger.getLogger(Receiver.class.getName()).log(Level.SEVERE, null, ex);
            }
    }
    
    public void accept(JSONObject otherJSON) throws UnknownHostException, IOException{
        JSONArray arr;
        try {
            arr = otherJSON.getJSONArray("proposer_id");
            int otherProposerProposalID = arr.getInt(0);
            int otherProposerID = arr.getInt(1);
            JSONObject OKToOtherProposer = new JSONObject();
            OKToOtherProposer.put("status", "ok");
            OKToOtherProposer.put("description", "accepted");
            buf = OKToOtherProposer.toString().getBytes();
            
            InetAddress address = InetAddress.getByAddress(listPlayers.getPlayer(otherProposerID).getAddress().getBytes());
            int port = Integer.parseInt(listPlayers.getPlayer(otherProposerID).getPort());
            sendData = new DatagramPacket(buf, buf.length, address,port);
            socketUDP.send(sendData);
        } catch (JSONException ex) {
            Logger.getLogger(Acceptor.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public boolean consensusPaxos(int count) {
        if (count > (listPlayers.getSize()/2 + 1)) {
            return true;
        }
        return false;
    }
    
    public void sendAcceptedProposal(DatagramSocket socketAccept, JSONObject request, int playerId) {
        try {
            byte[] buf = request.toString().getBytes();
            InetAddress address = InetAddress.getByAddress(listPlayers.getPlayer(playerId).getAddress().getBytes());
            int port = Integer.parseInt(listPlayers.getPlayer(playerId).getPort());
            sendData = new DatagramPacket(buf, buf.length, address,port);
            socketAccept.send(sendData);
        } catch (IOException ex) {
            Logger.getLogger(Proposer.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void broadcastAcceptedProposal() throws JSONException {
        DatagramSocket socketAccept;
        try {
            socketAccept = new DatagramSocket();
            JSONObject acceptedProposalJSON = new JSONObject();
            acceptedProposalJSON.put("method","accept_proposal");
            acceptedProposalJSON.put("proposal_id", new JSONArray(new Object[]{proposer.getProposalID(), playerID}));
            acceptedProposalJSON.put("kpu_id", playerID);

            for (int i = 0; i < listPlayers.getSize(); i++) {
                sendAcceptedProposal(socketAccept, acceptedProposalJSON, i);
            }
        } catch (SocketException ex) {
            Logger.getLogger(Acceptor.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}

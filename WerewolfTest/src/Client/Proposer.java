/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Client;

import Server.ListPlayer;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Arrays;
import static java.util.Arrays.sort;
import java.util.logging.Level;
import java.util.logging.Logger;
import static javax.swing.UIManager.put;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import static java.util.Arrays.sort;

/**
 *
 * @author asus
 */
public class Proposer implements Runnable{

    DatagramSocket socket;
    byte[] buf;
    DatagramPacket sendData;
    DatagramPacket receiveData;
    private ListPlayer listPlayers;
    private boolean isProposer;
    private int playerID;
    private int proposalID;
    
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
    
    public void prepareProposal() throws JSONException {
        JSONObject prepareProposalJSON = new JSONObject();
        prepareProposalJSON.put("method","prepare_proposal");
        prepareProposalJSON.put("proposal_id", new JSONArray(new Object[]{proposalID, playerID}));
        
        for (int i = 0; i < listPlayers.getSize(); i++) {
            if (listPlayers.getPlayer(i).getId() != playerID) {
                sendRequest(prepareProposalJSON, i);
            }
        }
    }
    
    public void sendRequest(JSONObject request, int playerId) {
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
    
    public Proposer(DatagramSocket _socket){
        try {
            getListClient();
        } catch (JSONException ex) {
            Logger.getLogger(Proposer.class.getName()).log(Level.SEVERE, null, ex);
        }
        
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
        
        socket = _socket;
        buf = new byte[1024];
        
        
        
        
    }
    
    @Override
    public void run() {
        while(true){
            System.out.println("Sender is started");
            if (isProposer) {
                try {
                    prepareProposal();
                } catch (JSONException ex) {
                    Logger.getLogger(Proposer.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
    }
    
}

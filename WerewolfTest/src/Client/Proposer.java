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
    private int playerID;
    private int proposalID = 0;
        
    public void prepareProposal() throws JSONException {
        JSONObject prepareProposalJSON = new JSONObject();
        prepareProposalJSON.put("method","prepare_proposal");
        prepareProposalJSON.put("proposal_id", new JSONArray(new Object[]{proposalID, playerID}));
        
        proposalID++;
        
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
        socket = _socket;
        buf = new byte[1024];
    }
    
    @Override
    public void run() {
        while(true){
            System.out.println("Proposer is started");
            try {
                prepareProposal();
            } catch (JSONException ex) {
                Logger.getLogger(Proposer.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
    
    //GETTER AND SETTER
    public DatagramSocket getSocket() {
        return socket;
    }

    public void setSocket(DatagramSocket socket) {
        this.socket = socket;
    }

    public byte[] getBuf() {
        return buf;
    }

    public void setBuf(byte[] buf) {
        this.buf = buf;
    }

    public DatagramPacket getSendData() {
        return sendData;
    }

    public void setSendData(DatagramPacket sendData) {
        this.sendData = sendData;
    }

    public DatagramPacket getReceiveData() {
        return receiveData;
    }

    public void setReceiveData(DatagramPacket receiveData) {
        this.receiveData = receiveData;
    }

    public ListPlayer getListPlayers() {
        return listPlayers;
    }

    public void setListPlayers(ListPlayer listPlayers) {
        this.listPlayers = listPlayers;
    }

    public int getPlayerID() {
        return playerID;
    }

    public void setPlayerID(int playerID) {
        this.playerID = playerID;
    }

    public int getProposalID() {
        return proposalID;
    }

    public void setProposalID(int proposalID) {
        this.proposalID = proposalID;
    }
    
    
}

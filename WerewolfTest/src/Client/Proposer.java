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
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Arrays;
import static java.util.Arrays.sort;
import java.util.logging.Level;
import java.util.logging.Logger;
import static javax.swing.UIManager.put;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import static java.util.Arrays.sort;
import java.util.concurrent.atomic.AtomicBoolean;

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
    AtomicBoolean isConsensusTime = new AtomicBoolean(true);
    AtomicBoolean isProposer = new AtomicBoolean(false);
    AtomicBoolean isSendRequestTime = new AtomicBoolean(false);
    AtomicBoolean isSendProposalTime = new AtomicBoolean(false);
    private ArrayList<Integer> listAcceptorReceive;
     
    public Proposer(ListPlayer _listPlayer, int _playerID) throws SocketException{
        socket = new DatagramSocket();
        this.listPlayers = _listPlayer;
        this.playerID = _playerID;
        listAcceptorReceive = new ArrayList<Integer>();
        buf = new byte[1024];
    }
    
    @Override
    public void run() {
        int i = 0;
        while(true){
            //System.err.println(getIsSendProposalTime() + " " + ++i);
            if(getIsConsensusTime()){
                if(getIsProposer()){
                    if(getIsSendProposalTime()){
                        try {
                            prepareProposal();
                        } catch (JSONException ex) {
                            Logger.getLogger(Proposer.class.getName()).log(Level.SEVERE, null, ex);
                        }
                        setIsSendProposalTime(false);
                    }
                    if(getIsSendRequestTime()){
                        try {
                            broadcastAcceptedProposal();
                        } catch (JSONException ex) {
                            Logger.getLogger(Proposer.class.getName()).log(Level.SEVERE, null, ex);
                        }
                        setIsSendRequestTime(false);
                    }
                }
            } else {
                break;
            }
        }
        return;
    }
    
    public void prepareProposal() throws JSONException {
        JSONObject prepareProposalJSON = new JSONObject();
        prepareProposalJSON.put("method","prepare_proposal");
        prepareProposalJSON.put("proposal_id", new JSONArray(new Object[]{proposalID, playerID}));
        for (int i = 0; i < listPlayers.getSize(); i++) {
            int playerDestinationId = listPlayers.getPlayer(i).getId();
            if (playerDestinationId != playerID && !listAcceptorReceive.contains(playerDestinationId)) {
                sendRequest(prepareProposalJSON, i);
            }
        }
    }
    
    public void sendRequest(JSONObject request, int playerId) {
        try {
            byte[] buf = request.toString().getBytes();
            InetAddress address = InetAddress.getByName(listPlayers.getPlayer(playerId).getAddress());
            int port = listPlayers.getPlayer(playerId).getPort();
            sendData = new DatagramPacket(buf, buf.length, address,port);
             System.out.println("proposer send : " + new String(sendData.getData()));
            socket.send(sendData);
        } catch (IOException ex) {
            Logger.getLogger(Proposer.class.getName()).log(Level.SEVERE, null, ex);
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

    public ListPlayer getListPlayerso() {
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

    /**
     * @param isConsensusTime the isConsensusTime to set
     */
    public void setIsConsensusTime(boolean isConsensusTime) {
        this.isConsensusTime.set(isConsensusTime);
    }
    
    public boolean getIsConsensusTime(){
        return isConsensusTime.get();
    }

    /**
     * @param isProposer the isProposer to set
     */
    public void setIsProposer(boolean isProposer) {
        this.isProposer.set(isProposer);
    }

    public boolean getIsProposer(){
        return isProposer.get();
    }
    
    /**
     * @return the isSendRequestTime
     */
    public boolean getIsSendRequestTime() {
        return isSendRequestTime.get();
    }

    /**
     * @param isSendRequestTime the isSendRequestTime to set
     */
    public void setIsSendRequestTime(boolean isSendRequestTime) {
        this.isSendRequestTime.set(isSendRequestTime);
    }

    /**
     * @return the isSendProposalTime
     */
    public boolean getIsSendProposalTime() {
        return isSendProposalTime.get();
    }

    /**
     * @param isSendProposalTime the isSendProposalTime to set
     */
    public void setIsSendProposalTime(boolean isSendProposalTime) {
        this.isSendProposalTime.set(isSendProposalTime);
    }
    
    public void sendAcceptedProposal(DatagramSocket socketAccept, JSONObject request, int playerId) {
        try {
            byte[] buf = request.toString().getBytes();
            InetAddress address = InetAddress.getByName(listPlayers.getPlayer(playerId).getAddress());
            int port = listPlayers.getPlayer(playerId).getPort();
            sendData = new DatagramPacket(buf, buf.length, address,port);
            System.out.println("proposer send : " + new String(sendData.getData()));
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
            acceptedProposalJSON.put("proposal_id", new JSONArray(new Object[]{getProposalID(), playerID}));
            acceptedProposalJSON.put("kpu_id", playerID);
            
            System.out.println("ini isi list : " + listAcceptorReceive.toString());
            for (int i = 0; i < listPlayers.getSize(); i++) {
                if (!listAcceptorReceive.contains(i)) {
                    sendAcceptedProposal(socketAccept, acceptedProposalJSON, i);
                }
            }
        } catch (SocketException ex) {
            Logger.getLogger(Acceptor.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public ArrayList<Integer> getListAcceptorReceive() {
        return this.listAcceptorReceive;
    }
    
    public void incrementProposalId(){
        proposalID++;
    }
}

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
    private boolean checkIsProposerTime = true;
    private boolean isKPU = false;
    private boolean isLeader = false;
    private boolean isLeaderSelected = false;
    private int playerID;
    private Proposer proposer;
    private Thread proposerth;
    private boolean isConsensusTime = true;
    private int counterPromiseNotProposer = 0;
    private int firstProposerProposalID, secondProposerProposalID, firstProposerID, secondProposerID;
    private int previousKPUId = -999;
    private int countConsesusPaxos = 0;
    private int countReceivePromise = 0;
    private int countReceiveAccept = 0;
    
    public Acceptor(DatagramSocket _socketUDP, ListPlayer _listPlayers, int _playerID) throws SocketException{
        this.socketUDP = _socketUDP;
        this.listPlayers = _listPlayers;
        this.playerID = _playerID;
        proposer = new Proposer(listPlayers, playerID);
        proposerth = new Thread(proposer);
    }
            
    @Override
    public void run() {
        while(true){
            if(isConsensusTime){
                if(checkIsProposerTime){
                    checkIsProposer();
                    checkIsProposerTime = false;
                    proposer.setIsSendProposalTime(true);
                }
                buf = new byte[1024];
                receiveData = new DatagramPacket(buf, buf.length);
                try {
                    if (isProposer) {
                        
                    }
                    socketUDP.receive(receiveData);
                    System.err.println("acceptor receive : " + new String(receiveData.getData()));
                    String otherProposer = new String(receiveData.getData(), 0, receiveData.getLength());
                    JSONObject otherJSON = new JSONObject(otherProposer);
                    if(otherJSON.has("method")){
                        if(otherJSON.getString("method").equals("prepare_proposal")){
                            promise(otherJSON);
                        }
                        if(otherJSON.getString("method").equals("accept_proposal")){
                            accept(otherJSON);
                        }
                    }
                    if(isProposer){
                        if(otherJSON.has("status")){
                            if(!isLeaderSelected){
                                
                                countReceivePromise++;
                                if(otherJSON.getString("status").equals("ok")) countConsesusPaxos++;
                                if(countReceivePromise == listPlayers.getSize()-1) {
                                    if(consensusPaxos(countConsesusPaxos)) isLeader = true;
                                    countConsesusPaxos = 0;
                                    if(isLeader){
                                        proposer.setIsSendRequestTime(true);
                                    }
                                    isLeaderSelected = true;
                                }
                            }
                            if(isLeader){
                                countReceiveAccept++;
                                if(otherJSON.getString("status").equals("ok")) countConsesusPaxos++;
                                if(countReceiveAccept == listPlayers.getSize()-1){
                                    if(consensusPaxos(countConsesusPaxos)){
                                        //proposerth.stop();
                                        setIsConsensusTime(false);
                                        proposer.setIsConsensusTime(false);
                                        isKPU = true;
                                        isLeader = false;
                                    } else {
                                        proposer.setIsSendRequestTime(true);
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
                
            } else if (!isConsensusTime) {
                if(isKPU){
                    try {
                        KPU kpu = new KPU(socketUDP,playerID,listPlayers);
                        Thread kpuThread = new Thread(kpu);
                        kpuThread.start();
                    } catch (JSONException ex) {
                        Logger.getLogger(Acceptor.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
                try {
                    socketUDP.receive(receiveData);
                    if (receiveData != null) {
                        JSONObject json = new JSONObject(new String(receiveData.getData()));
                        while(json.has("kpu_id")) {
                            System.err.println("acceptor receive : " + new String(receiveData.getData()));
                            int kpuID = new JSONObject(new String(receiveData.getData())).getInt("kpu_id");
                            InetAddress address = InetAddress.getByName(listPlayers.getPlayer(kpuID).getAddress());
                            int port = listPlayers.getPlayer(kpuID).getPort();

                            JSONObject resKPU = new JSONObject();
                            resKPU.put("status", "ok");
                            resKPU.put("description", "accepted");

                            buf = resKPU.toString().getBytes();
                            sendData = new DatagramPacket(buf, buf.length, address,port);
                            System.out.println("acceptor send : " + new String(sendData.getData()));
                            socketUDP.send(sendData);

                        }
                    }
                    
                } catch (JSONException ex) {
                    Logger.getLogger(Acceptor.class.getName()).log(Level.SEVERE, null, ex);
                } catch (IOException ex) {
                    Logger.getLogger(Acceptor.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            
            
        }
    }
    
    public void checkIsProposer(){
        int[] listPlayerID = new int[6];
        
        for (int i = 0; i < listPlayers.getSize(); i++) {
            listPlayerID[i] = listPlayers.getPlayer(i).getId();
        }
        
        Arrays.sort(listPlayerID);
        
        if (playerID == listPlayerID[listPlayerID.length-1] || playerID == listPlayerID[listPlayerID.length-2]) {
            proposerth.start();
            isProposer = true;
            proposer.setIsProposer(true);
            
        } else {
            isProposer = false;
            proposer.setIsProposer(false);
        }
    }
            
    public void promise(JSONObject otherJSON) throws JSONException {
        try {                
            if (isProposer) {
                JSONArray arr = otherJSON.getJSONArray("proposal_id");
                int otherProposerProposalID = arr.getInt(0);
                int otherProposerID = arr.getInt(1);
                JSONObject OKToOtherProposer = new JSONObject();
                OKToOtherProposer.put("status", "ok");
                OKToOtherProposer.put("description", "accepted");
                if(previousKPUId != -999) OKToOtherProposer.put("previous_accepted", previousKPUId); 

                buf = OKToOtherProposer.toString().getBytes();
                InetAddress address = InetAddress.getByName(listPlayers.getPlayer(otherProposerID).getAddress());
                int port = listPlayers.getPlayer(otherProposerID).getPort();
                sendData = new DatagramPacket(buf, buf.length, address,port);
                socketUDP.send(sendData);
            } else { //Harus nunggu 2 proposal
                ++counterPromiseNotProposer;
                JSONArray arr = otherJSON.getJSONArray("proposal_id");
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
                        InetAddress address = InetAddress.getByName(listPlayers.getPlayer(secondProposerID).getAddress());
                        int port = listPlayers.getPlayer(secondProposerID).getPort();
                        sendData = new DatagramPacket(buf, buf.length, address,port);
                        socketUDP.send(sendData);

                        buf = failResponse.toString().getBytes();
                        address = InetAddress.getByName(listPlayers.getPlayer(firstProposerID).getAddress());
                        port = listPlayers.getPlayer(firstProposerID).getPort();
                        sendData = new DatagramPacket(buf, buf.length, address,port);
                        System.out.println("acceptor send : " + new String(sendData.getData()));
                        socketUDP.send(sendData);
                    } else if ((firstProposerProposalID > secondProposerProposalID)||((firstProposerProposalID == secondProposerProposalID)
                            && (firstProposerID > secondProposerID))){
                        buf = okResponse.toString().getBytes();
                        InetAddress address = InetAddress.getByName(listPlayers.getPlayer(firstProposerID).getAddress());
                        int port = listPlayers.getPlayer(firstProposerID).getPort();
                        sendData = new DatagramPacket(buf, buf.length, address,port);
                        socketUDP.send(sendData);

                        buf = failResponse.toString().getBytes();
                        address = InetAddress.getByName(listPlayers.getPlayer(secondProposerID).getAddress());
                        port = listPlayers.getPlayer(secondProposerID).getPort();
                        sendData = new DatagramPacket(buf, buf.length, address,port);
                        System.out.println("acceptor send : " + new String(sendData.getData()));
                        socketUDP.send(sendData);
                    } else {
                        countConsesusPaxos = 0;
                        countReceivePromise = 0;
                        isLeaderSelected = false;
                        proposer.setIsSendProposalTime(true);
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
            
            InetAddress address = InetAddress.getByName(listPlayers.getPlayer(otherProposerID).getAddress());
            int port = listPlayers.getPlayer(otherProposerID).getPort();
            sendData = new DatagramPacket(buf, buf.length, address,port);
            System.out.println("acceptor send : " + new String(sendData.getData()));
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
    
    /**
     * @param isConsensusTime the isConsensusTime to set
     */
    public void setIsConsensusTime(boolean isConsensusTime) {
        this.isConsensusTime = isConsensusTime;
        if(isConsensusTime) checkIsProposerTime = true;
    }
    
    //GETTER AND SETTER
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

    public boolean isIsKPU() {
        return isKPU;
    }

    public void setIsKPU(boolean isKPU) {
        this.isKPU = isKPU;
    }
    
    
}

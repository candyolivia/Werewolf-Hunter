/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Client;

import Server.ListPlayer;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
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
    private int countReceiveFromWerewolf = 0;
    private int countReceiveForDay = 0;
    private Map<Integer, Integer> votedByWerewolf;
    private Map<Integer, Integer> votedForDay;
    private Socket socketServer;
    
    
    public Acceptor(DatagramSocket _socketUDP, ListPlayer _listPlayers, int _playerID, Socket _sock) throws SocketException{
        this.socketUDP = _socketUDP;
        this.listPlayers = _listPlayers;
        this.playerID = _playerID;
        votedForDay = new HashMap<Integer, Integer>();
        votedByWerewolf = new HashMap<Integer, Integer>();
        proposer = new Proposer(listPlayers, playerID);
        this.socketServer = _sock;
    }
            
    @Override
    public void run() {
        while(true){
            if(isConsensusTime){
                if(checkIsProposerTime){
                    countReceivePromise = 0;
                    countReceiveAccept = 0;
                    countConsesusPaxos = 0;
                    checkIsProposer();
                    isLeader = false;
                    isLeaderSelected = false;
                    checkIsProposerTime = false;
                    proposer.setIsSendProposalTime(true);
                }
                buf = new byte[1024];
                receiveData = new DatagramPacket(buf, buf.length);
                try {
                    socketUDP.receive(receiveData);
                    //System.err.println("acceptor receive : " + new String(receiveData.getData()));
                    String otherProposer = new String(receiveData.getData(), 0, receiveData.getLength());
                    JSONObject otherJSON = new JSONObject(otherProposer);
                    if(otherJSON.has("method")){
                        if(otherJSON.getString("method").equals("prepare_proposal")){
                            promise(otherJSON);
                        }
                        if(otherJSON.getString("method").equals("accept_proposal")){
                            if(!isLeader) {
                                //socketUDP.setSoTimeout(0);
                                isLeaderSelected = false;
                            }
                            accept(otherJSON);
                        }
                    }
                    if(getIsProposer()){
                        if(otherJSON.has("status")){
                            if(!isLeaderSelected){
                                countReceivePromise++;
                                proposer.getListAcceptorReceive().add(listPlayers.getPlayerId(receiveData.getAddress().toString().substring(1), receiveData.getPort()));
                                if(otherJSON.getString("status").equals("ok")) {
                                    countConsesusPaxos++;
                                }
                                if(countReceivePromise == listPlayers.getSize()-1) {
                                    proposer.incrementProposalId();
                                    if(consensusPaxos(countConsesusPaxos)) isLeader = true;
                                    countConsesusPaxos = 0;
                                    proposer.getListAcceptorReceive().clear();
                                    isLeaderSelected = true;
                                    if(isLeader){
                                        //System.out.println("jadi leader");
                                        proposer.setProposalID(proposer.getProposalID()-1);
                                        proposer.setIsSendRequestTime(true);
                                    }
                                }
                            } else if(isLeader){
                                //System.out.println("masuk isleader");
                                countReceiveAccept++;
                                proposer.getListAcceptorReceive().add(listPlayers.getPlayerId(receiveData.getAddress().toString().substring(1), receiveData.getPort()));
                                if(otherJSON.getString("status").equals("ok")) countConsesusPaxos++;
                                if(countReceiveAccept == listPlayers.getSize()){
                                    //System.out.println("masuk receive accept");
                                    if(consensusPaxos(countConsesusPaxos)){
                                        //System.out.println("masuk receive consensus accept");
                                        proposer.incrementProposalId();
                                        //proposerth.stop();
                                        setIsKPU(true);
                                        isLeader = false;
                                        //System.out.println("jadi kpu");
                                        socketUDP.setSoTimeout(5000);
                                        setIsConsensusTime(false);
                                        //socketUDP.setSoTimeout(0);
                                        proposer.getListAcceptorReceive().clear();
                                        proposerth.join();
                                    }
                                }
                            }
                        }
                    }
                } catch (SocketTimeoutException e) {
                    //System.out.println("masuk timeout");
                    //System.err.println("ini jumlah dalam list diterima : " + proposer.getListAcceptorReceive().size());
                    if(getIsProposer() && !isLeader){
                        if(countReceivePromise < listPlayers.getSize()-1){
                            System.out.println("masuk sini ngirim proposal lagi");
                            proposer.setIsSendProposalTime(true);
                        }
                    }
                    if(isLeader){
                        if(countReceiveAccept < listPlayers.getSize()){
                            System.out.println("masuk sini ngirim accept lagi");
                            proposer.setIsSendRequestTime(true);
                        }
                    }
                    continue;
                } catch (IOException ex) {
                    Logger.getLogger(Acceptor.class.getName()).log(Level.SEVERE, null, ex);
                } catch (JSONException ex) {
                    Logger.getLogger(Acceptor.class.getName()).log(Level.SEVERE, null, ex);
                } catch (InterruptedException ex) {
                    Logger.getLogger(Acceptor.class.getName()).log(Level.SEVERE, null, ex);
                }
                
            } else if(isKPU){
                //System.err.println("jadi aaaa kpu");
                buf = new byte[1024];
                receiveData = new DatagramPacket(buf, buf.length);
                try {
                    //System.err.println("jancuk1");
                    socketUDP.receive(receiveData);
                    //System.err.println("jancuk2");
                    System.out.println("kpu receive : " + new String(receiveData.getData()));
                    String otherProposer = new String(receiveData.getData(), 0, receiveData.getLength());
                    JSONObject otherJSON = new JSONObject(otherProposer);
                    if(otherJSON.has("method")){
                        if(otherJSON.getString("method").equals("vote_werewolf")){
                            int valueVoted = otherJSON.getInt("player_id");
                            if(votedByWerewolf.containsKey(valueVoted)){
                                votedByWerewolf.put(valueVoted,votedByWerewolf.get(valueVoted)+1);
                            } else {
                                votedByWerewolf.put(valueVoted, 1);
                            }
                            countReceiveFromWerewolf++;
                            if(countReceiveFromWerewolf == listPlayers.getWerewolfAlive()){
                                JSONObject responseToClient = new JSONObject();
                                JSONArray votedByClient = new JSONArray();
                                JSONObject requestToServer = new JSONObject();
                                requestToServer.put("method", "vote_result_werewolf");
                                //System.out.println("jumlah werewolf hidup : " + listPlayers.getWerewolfAlive());
                                if(votedByWerewolf.get(valueVoted) == listPlayers.getWerewolfAlive()){
                                    responseToClient.put("status", "ok");
                                    responseToClient.put("description", "");
                                    requestToServer.put("vote_status", 1);
                                    requestToServer.put("player_killed", valueVoted);
                                } else {
                                    responseToClient.put("status", "fail");
                                    responseToClient.put("description", "");
                                    requestToServer.put("vote_status", -1);
                                }
                                buf = responseToClient.toString().getBytes();
                                for(int i = 0; i < listPlayers.getSize(); ++i){
                                    if(listPlayers.getPlayer(i).getRole().equals("werewolf") && listPlayers.getPlayer(i).getAlive() == 1){
                                        InetAddress address = InetAddress.getByName(listPlayers.getPlayer(i).getAddress());
                                        int port = listPlayers.getPlayer(i).getPort();
                                        sendData = new DatagramPacket(buf, buf.length, address, port);
                                        socketUDP.send(sendData);
                                    }
                                }

                                for(Map.Entry<Integer, Integer> entry : votedByWerewolf.entrySet()){
                                    JSONArray temp = new JSONArray();
                                    temp.put(entry.getKey());
                                    temp.put(entry.getValue());
                                    votedByClient.put(temp);
                                }
                                requestToServer.put("vote_result", votedByClient);
                                PrintWriter out = new PrintWriter(socketServer.getOutputStream(), true);
                                out.println(requestToServer);
                                countReceiveFromWerewolf = 0;
                                votedByWerewolf.clear();
                            }
                        } else if(otherJSON.getString("method").equals("vote_civilian")){
                            int valueVoted = otherJSON.getInt("player_id");
                            if(votedForDay.containsKey(valueVoted)){
                                votedForDay.put(valueVoted,votedForDay.get(valueVoted)+1);
                            } else {
                                votedForDay.put(valueVoted, 1);
                            }
                            countReceiveForDay++;
                            //System.out.println("jumlah coutreceiveforday :" + countReceiveForDay);
                            //System.out.println("jumlah pemain hidup :" + listPlayers.getPlayersAlive());
                            if(countReceiveForDay == listPlayers.getPlayersAlive()){
                                JSONObject responseToClient = new JSONObject();
                                JSONArray votedByClient = new JSONArray();
                                JSONObject requestToServer = new JSONObject();
                                requestToServer.put("method", "vote_result_civilian");
                                int resultConsensus = consensusPaxosForDay(votedForDay);
                                if(resultConsensus > -999){
                                    responseToClient.put("status", "ok");
                                    responseToClient.put("description", "");
                                    requestToServer.put("vote_status", 1);
                                    requestToServer.put("player_killed", resultConsensus);
                                } else {
                                    responseToClient.put("status", "fail");
                                    responseToClient.put("description", "");
                                    requestToServer.put("vote_status", -1);
                                }

                                buf = responseToClient.toString().getBytes();
                                for(int i = 0; i < listPlayers.getSize(); ++i){
                                    if(listPlayers.getPlayer(i).getAlive() == 1){
                                        InetAddress address = InetAddress.getByName(listPlayers.getPlayer(i).getAddress());
                                        int port = listPlayers.getPlayer(i).getPort();
                                        sendData = new DatagramPacket(buf, buf.length, address, port);
                                        socketUDP.send(sendData);
                                    }
                                }
                                
                                for(Map.Entry<Integer, Integer> entry : votedForDay.entrySet()){
                                    JSONArray temp = new JSONArray();
                                    temp.put(entry.getKey());
                                    temp.put(entry.getValue());
                                    votedByClient.put(temp);
                                }
                                requestToServer.put("vote_result", votedByClient);
                                PrintWriter out = new PrintWriter(socketServer.getOutputStream(), true);
                                out.println(requestToServer);
                                countReceiveForDay = 0;
                                votedForDay.clear();
                            }
                        }
                    } else if(otherJSON.has("status")) {
                        if(otherJSON.getString("status").equals("ok")) {
                            
                        } else if(otherJSON.getString("status").equals("fail")) {
                            
                        }
                    }
                } catch (SocketTimeoutException e) {
                    continue;
                } catch (IOException ex) {
                    Logger.getLogger(Acceptor.class.getName()).log(Level.SEVERE, null, ex);
                } catch (JSONException ex) {
                    Logger.getLogger(Acceptor.class.getName()).log(Level.SEVERE, null, ex);
                }
            } else {
                //System.err.println("bukan kpu");
                buf = new byte[1024];
                receiveData = new DatagramPacket(buf, buf.length);
                try {
                    socketUDP.receive(receiveData);
                    System.out.println("dari kpu : " + new String(receiveData.getData()));
                    String dataIn = new String(receiveData.getData(), 0, receiveData.getLength());
                    JSONObject otherJSON = new JSONObject(dataIn);
                    if(otherJSON.has("status")){
                        if(otherJSON.getString("status").equals("ok")) {
                            
                        } else if(otherJSON.getString("status").equals("fail")) {
                            
                        }
                    }
                } catch (SocketTimeoutException e) {
                    continue;
                } catch (IOException ex) {
                    Logger.getLogger(Acceptor.class.getName()).log(Level.SEVERE, null, ex);
                } catch (JSONException ex) {
                    Logger.getLogger(Acceptor.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
    }
    
    public void checkIsProposer(){
        System.out.println("check proposer lagi");
        int[] listPlayerID = new int[6];
        
        for (int i = 0; i < listPlayers.getSize(); i++) {
            listPlayerID[i] = listPlayers.getPlayer(i).getId();
        }
        
        Arrays.sort(listPlayerID);
        
        if (playerID == listPlayerID[listPlayerID.length-1] || playerID == listPlayerID[listPlayerID.length-2]) {
            proposerth = new Thread(proposer);
            proposerth.start();
            try {
                socketUDP.setSoTimeout(5000);
            } catch (SocketException ex) {
                Logger.getLogger(Acceptor.class.getName()).log(Level.SEVERE, null, ex);
            }
            setIsProposer(true);
        } else {
            setIsProposer(false);
            try {
                socketUDP.setSoTimeout(0);
            } catch (SocketException ex) {
                Logger.getLogger(Acceptor.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
            
    public void promise(JSONObject otherJSON) throws JSONException {
        try {                
            if (getIsProposer()) {
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
                    if(secondProposerID == firstProposerID) counterPromiseNotProposer = 1;
                }
                if(counterPromiseNotProposer == 2){
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
                        //System.out.println("acceptor send : " + new String(sendData.getData()));
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
                        //System.out.println("acceptor send : " + new String(sendData.getData()));
                        socketUDP.send(sendData);
                    }
                    counterPromiseNotProposer = 0;
                }
            }
        } catch (IOException ex) {
            Logger.getLogger(Receiver.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public void accept(JSONObject otherJSON) throws UnknownHostException, IOException{
        JSONArray arr;
        try {
            arr = otherJSON.getJSONArray("proposal_id");
            int otherProposerProposalID = arr.getInt(0);
            int otherProposerID = arr.getInt(1);
            JSONObject OKToOtherProposer = new JSONObject();
            OKToOtherProposer.put("status", "ok");
            OKToOtherProposer.put("description", "accepted");
            buf = OKToOtherProposer.toString().getBytes();
            
            InetAddress address = InetAddress.getByName(listPlayers.getPlayer(otherProposerID).getAddress());
            int port = listPlayers.getPlayer(otherProposerID).getPort();
            sendData = new DatagramPacket(buf, buf.length, address,port);
            //System.out.println("acceptor send : " + new String(sendData.getData()));
            socketUDP.send(sendData);
            
            JSONObject sendDataJSON = new JSONObject(new String(sendData.getData()));
            int kpu_id = otherJSON.getInt("kpu_id");
            
            PrintWriter out = new PrintWriter(socketServer.getOutputStream(), true);
            JSONObject kpuSelected = new JSONObject();
            kpuSelected.put("method", "accepted_proposal");
            kpuSelected.put("kpu_id", kpu_id);
            kpuSelected.put("description", "Kpu is selected");
            //System.out.println("KPU : " + kpuSelected.toString());
            out.println(kpuSelected);
            if(!isLeader) {
                socketUDP.setSoTimeout(5000);
                setIsConsensusTime(false);
                if(getIsProposer()){
                    proposerth.join();
                }
            }
        } catch (JSONException ex) {
            Logger.getLogger(Acceptor.class.getName()).log(Level.SEVERE, null, ex);
        } catch (InterruptedException ex) {
            Logger.getLogger(Acceptor.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public boolean consensusPaxos(int count) {
        if (count >= (listPlayers.getSize()/2 + 1)) {
            return true;
        }
        return false;
    }
    
    public int consensusPaxosForDay(Map<Integer, Integer> vote) {
        for(Map.Entry<Integer, Integer> entry : vote.entrySet()){
            if (entry.getValue() >= (listPlayers.getPlayersAlive()/2 + 1)) {
                return entry.getKey();
            }
        }
        return -999;
    }
    
    /**
     * @param isConsensusTime the isConsensusTime to set
     */
    public void setIsConsensusTime(boolean isConsensusTime) {
        this.isConsensusTime = isConsensusTime;
        proposer.setIsConsensusTime(isConsensusTime);
        if(isConsensusTime) {
            checkIsProposerTime = true;
            setIsKPU(false);
        }
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

    /**
     * @return the isProposer
     */
    public boolean getIsProposer() {
        return isProposer;
    }

    /**
     * @param isProposer the isProposer to set
     */
    public void setIsProposer(boolean isProposer) {
        this.isProposer = isProposer;
        proposer.setIsProposer(isProposer);
    }
}

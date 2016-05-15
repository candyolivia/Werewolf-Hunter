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
import java.io.PrintWriter;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JFrame;
import javax.swing.JOptionPane;

import org.json.*;

/**
 *
 * @author Candy
 */
public class Client {
    DatagramSocket socketUDP;
    private boolean isWerewolf;
    private Acceptor acceptor;
    private Thread acceptorThread;
    private boolean isDay;
    private String hostName;
    private int portNumber;
    private boolean valid;
    private ListPlayer listPlayers;
    private int playerID;
    private String clientAddress;
    private int clientPort;
    private boolean isStart = false;
    private Socket werewolfSocket;
    private int idKpu;
    
    
    
    public Client() throws IOException{
        initializeClient();
        werewolfSocket = new Socket(hostName, portNumber);
        try (
            PrintWriter out = new PrintWriter(werewolfSocket.getOutputStream(), true);
            BufferedReader in = new BufferedReader(
                new InputStreamReader(werewolfSocket.getInputStream()));
        ) {
            
            BufferedReader stdIn =
                new BufferedReader(new InputStreamReader(System.in));
            String fromServer;
            String fromUser;
            
            String inputUsername = (String)JOptionPane.showInputDialog(
                                new JFrame(),
                                "Enter Username:\n",
                                "Enter Username",
                                JOptionPane.QUESTION_MESSAGE);
            
            
//            do {
//                clientAddress = (String)JOptionPane.showInputDialog(
//                                new JFrame(),
//                                "Enter clientAddress:\n",
//                                "Enter clientAddress",
//                                JOptionPane.QUESTION_MESSAGE);
//            } while (clientAddress == null);
                clientAddress = "127.0.0.1";
            
            do {
                clientPort = Integer.parseInt((String)JOptionPane.showInputDialog(
                                new JFrame(),
                                "Enter clientPort:\n",
                                "Enter clientPort",
                                JOptionPane.QUESTION_MESSAGE));
            } while (clientPort == 0);
            
            while (inputUsername != null) {
                try {
                    JSONObject obj = new JSONObject();
                    obj.put("method", "join");
                    obj.put("username",inputUsername);
                    obj.put("udp_address",clientAddress);
                    obj.put("udp_port", clientPort);
                    System.out.println("Client: " + obj);
                    out.println(obj);
                    
                    fromServer = in.readLine();
                    
                    JSONObject serverJSON = new JSONObject(fromServer);
                    
                    if (fromServer != null) {
                        System.out.println("Server:1 " + serverJSON);
                        if (serverJSON.get("status").equals("ok")){
                            playerID = serverJSON.getInt("player_id");
                            valid = true;
                            break;
                        } else if (serverJSON.get("status").equals("fail")||serverJSON.get("status").equals("error")){
                            System.out.println(serverJSON.get("description"));
                            break;
                        }
                        
                    }
                    
                } catch (JSONException ex) {
                    Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
                }
                
            }
            
            
            GameView game = new GameView();
            if (valid){
                java.awt.EventQueue.invokeLater(new Runnable() {
                    public void run() {
                        game.setVisible(true);
                        game.setOut(out);
                        game.setIn(in);
                    }
                });
            }
            
            while (valid) {
                fromServer = in.readLine();

                JSONObject serverJSON = new JSONObject(fromServer);
                JSONObject response = null;

                if (fromServer != null) {
                    System.out.println("Server:2 " + serverJSON);
                    if (serverJSON.has("method")&&!isStart){
                        JSONObject msg = new JSONObject();
                        switch(serverJSON.getString("method")){
                            case "": break;
                            case "start":
                                //olah status
                                game.startGame(inputUsername, serverJSON.getString("role"));
                                msg = new JSONObject();
                                msg.put("method", "client_address");
                                System.out.println("Client: " + msg);
                                out.println(msg);
                                response = getResponse(in);
                                game.updatePlayerList(getUsernames(response), getActivePlayers(response));
                                if (response.has("status")){
                                    //System.out.println("masuk sini");
                                    if (response.getString("status").equals("ok")) {
                                        socketUDP = new DatagramSocket(clientPort, InetAddress.getByName(clientAddress));
                                        //socketUDP.setSoTimeout(5000);
                                        acceptor = new Acceptor(socketUDP, listPlayers, playerID, werewolfSocket);
                                        acceptorThread = new Thread(acceptor);
                                        acceptorThread.start();
                                    }
                                }
                                //System.out.println("masuk sini3");
                                break;
                            case "vote_now":
                                if(serverJSON.getString("phase").equals("day"))
                                    voteCivilian();
                                else
                                    voteWerewolf();
                                break;
                            case "kpu_selected":
                                idKpu = serverJSON.getInt("kpu_id");
                                sendStatusOK(werewolfSocket);
                                break;
                            case "change_phase":
                                if(serverJSON.getString("time").equals("day")){
                                    isDay = true;
                                    updateListPlayers(werewolfSocket);
                                    acceptor.setIsConsensusTime(true);
                                } else {
                                    isDay = false;
                                    updateListPlayers(werewolfSocket);
                                    sendStatusOK(werewolfSocket);
                                }
                                break;
                            case "game_over":
                                break;
                        }
                    } 
                }
            }
        } catch (UnknownHostException e) {
            JOptionPane.showMessageDialog(new JFrame(), "Unknown Host Name: " +
                hostName + ".", "Error", JOptionPane.ERROR_MESSAGE);
            System.err.println("Don't know about host " + hostName);
            System.exit(1);
        } catch (IOException e) {
            JOptionPane.showMessageDialog(new JFrame(), "Can't establish connection to " +
                hostName + ".", "Error", JOptionPane.ERROR_MESSAGE);
            System.err.println("Couldn't get I/O for the connection to " +
                hostName);
            System.exit(1);
        } catch (JSONException ex) {
            Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
        }      
        
    }
    
    public void sendStatusOK(Socket sock) throws JSONException, IOException {
        PrintWriter out = new PrintWriter(sock.getOutputStream(), true);
        JSONObject json = new JSONObject();
        json.put("status", "ok");
        json.put("description", "ready to vote");
        out.println(json);
    }
    
    public void updateListPlayers(Socket sock) throws IOException, JSONException{
        PrintWriter out = new PrintWriter(sock.getOutputStream(), true);
        BufferedReader in = new BufferedReader(new InputStreamReader(sock.getInputStream()));
        JSONObject json = new JSONObject();
        json.put("method", "client_address");
        out.println(json);
        String fromServer = in.readLine();
        JSONObject serverJSON = new JSONObject(fromServer);
        getActivePlayers(serverJSON);
    }
    
    public void initializeClient(){
//        String inputHostname = (String)JOptionPane.showInputDialog(
//                                new JFrame(),
//                                "Enter Host Name:\n",
//                                "Enter Host Name",
//                                JOptionPane.QUESTION_MESSAGE);
//        
//        String inputPortNumber = (String)JOptionPane.showInputDialog(
//                                new JFrame(),
//                                "Enter Port Number:\n",
//                                "Enter Port Number",
//                                JOptionPane.QUESTION_MESSAGE);
        
//        hostName = inputHostname;
//        portNumber = Integer.parseInt(inputPortNumber);
       hostName = "127.0.0.1";
       portNumber = 8005;
        valid = false;
    }
    
    private static JSONObject getResponse(BufferedReader in) throws JSONException, IOException{
        JSONObject serverJSON = null;

            String fromServer = in.readLine();

            while (fromServer == null){
                fromServer = in.readLine();
            }

            serverJSON = new JSONObject(fromServer);
            System.out.println("Server:3 " + serverJSON);
            
        return serverJSON;
    }
    
    private ArrayList getUsernames(JSONObject response){
        System.out.println(response);
        try {
            ArrayList<String> usernames = new ArrayList<String>();
            listPlayers = new ListPlayer();
            JSONArray client = response.getJSONArray("clients");
            for (int i = 0; i < client.length(); i++) {
                int playerId = response.getJSONArray("clients").getJSONObject(i).getInt("player_id");
                String username = response.getJSONArray("clients").getJSONObject(i).getString("username");
                String address = response.getJSONArray("clients").getJSONObject(i).getString("address");
                int port = response.getJSONArray("clients").getJSONObject(i).getInt("port");
                if(response.getJSONArray("clients").getJSONObject(i).has("role") && response.getJSONArray("clients").getJSONObject(i).getString("role").equals("werewolf"))
                    listPlayers.addPlayer(playerId, username, address, port, werewolfSocket, true);
                else
                    listPlayers.addPlayer(playerId, username, address, port, werewolfSocket, false);
                usernames.add(username);
            }
            
            return usernames;
        } catch (JSONException ex) {
            Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
        
            return null;
        }
    }
    
    private ArrayList getActivePlayers(JSONObject response){
        System.out.println(response);
        try {
            ArrayList<String> players = new ArrayList<String>();
            JSONArray client = response.getJSONArray("clients");
            for (int i = 0; i < client.length(); i++) {
               
                String username = response.getJSONArray("clients").getJSONObject(i).getString("username");
                
                if (response.getJSONArray("clients").getJSONObject(i).getInt("is_alive") == 1){
                    players.add(username);
                    //System.out.println(username + " masih hidup, role di listplayer client : " + listPlayers.getPlayer(i).getRole());
                }
                else {
                    listPlayers.getPlayer(response.getJSONArray("clients").getJSONObject(i).getInt("player_id")).setAlive(0);
                    //System.out.println(username + " masih mati, role di listplayer client : " + listPlayers.getPlayer(i).getRole());
                }
            }
            
            return players;
        } catch (JSONException ex) {
            Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
        
            return null;
        }
    }
    
    public static void main(String[] args) throws IOException{
        Client c = new Client();
    }
    
    private void voteCivilian() throws JSONException, UnknownHostException, IOException{
        if(listPlayers.getPlayer(playerID).getAlive() == 1){
            Scanner reader = new Scanner(System.in);
            System.out.println("masukkan pemain yang ingin di vote : ");
            String pemainDipilih = reader.next();
            int pemainDipilihId = listPlayers.getPlayerId(pemainDipilih);
            JSONObject msg = new JSONObject();
            msg.put("method", "vote_civilian");
            msg.put("player_id", pemainDipilihId);
            byte[] buf = msg.toString().getBytes();
            InetAddress address = InetAddress.getByName(listPlayers.getPlayer(idKpu).getAddress());
            int port = listPlayers.getPlayer(idKpu).getPort();
            DatagramPacket sendPacket = new DatagramPacket(buf, buf.length, address, port);
            System.out.println("client send : " + new String(sendPacket.getData()));
            socketUDP.send(sendPacket);
        }
    }
    
    private void voteWerewolf() throws JSONException, UnknownHostException, IOException{
        if(listPlayers.getPlayer(playerID).getRole().equals("werewolf") && listPlayers.getPlayer(playerID).getAlive() == 1){
            Scanner reader = new Scanner(System.in);
            System.out.println("masukkan pemain yang ingin di vote : ");
            String pemainDipilih = reader.next();
            System.out.println("pemain yang dipilih : " + pemainDipilih);
            int pemainDipilihId = listPlayers.getPlayerId(pemainDipilih);
            JSONObject msg = new JSONObject();
            msg.put("method", "vote_werewolf");
            msg.put("player_id", pemainDipilihId);
            byte[] buf = msg.toString().getBytes();
            InetAddress address = InetAddress.getByName(listPlayers.getPlayer(idKpu).getAddress());
            int port = listPlayers.getPlayer(idKpu).getPort();
            DatagramPacket sendPacket = new DatagramPacket(buf, buf.length, address, port);
            System.out.println("client send : " + new String(sendPacket.getData()));
            socketUDP.send(sendPacket);
        }
    }
    
}

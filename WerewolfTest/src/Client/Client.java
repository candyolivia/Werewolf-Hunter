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
    
    
    public Client(){
        initializeClient();
        try (
            Socket kkSocket = new Socket(hostName, portNumber);
            PrintWriter out = new PrintWriter(kkSocket.getOutputStream(), true);
            BufferedReader in = new BufferedReader(
                new InputStreamReader(kkSocket.getInputStream()));
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
                        System.out.println("Server: " + serverJSON);
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
                    System.out.println("Server: " + serverJSON);
                    if (serverJSON.has("method")&&!isStart){
                        switch(serverJSON.getString("method")){
                            case "": break;
                            case "start":
                                //olah status
                                JSONObject msg = new JSONObject();
                                msg.put("method", "client_address");
                                System.out.println("Client: " + msg);
                                out.println(msg);
                                response = getResponse(in);
                                game.updatePlayerList(getUsernames(response));
                                if (response.has("status")){
                                    System.out.println("masuk sini");
                                    if (response.getString("status").equals("ok")) {
                                        socketUDP = new DatagramSocket(clientPort, InetAddress.getByName(clientAddress));
                                        acceptor = new Acceptor(socketUDP, listPlayers, playerID);
                                        acceptorThread = new Thread(acceptor);
                                        acceptorThread.start();
                                    }
                                }
                                System.out.println("masuk sini3");
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
       hostName = "localhost";
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
            System.out.println("Server: " + serverJSON);
            
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
                listPlayers.addPlayer(playerId, username, address, port);
                usernames.add(username);
            }
            
            return usernames;
        } catch (JSONException ex) {
            Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
        
            return null;
        }
    }
    
    public static void main(String[] args) throws IOException{
        Client c = new Client();
    }
    
}

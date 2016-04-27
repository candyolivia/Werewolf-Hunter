/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.json.JSONException;
import org.json.JSONObject;

/**
 *
 * @author Candy
 */
public class ServerThread extends Thread {
    private Socket clientSocket = null;
    private int playerId;
    private ListPlayer listPlayer;
    private boolean playing = false;
    private String username;

    public ServerThread(Socket socket) {
        super("ServerThread");
        this.clientSocket = socket;
    }
    
    public void run() {
        boolean valid = false;
        
        try ( 
        PrintWriter out =
            new PrintWriter(clientSocket.getOutputStream(), true);
        BufferedReader in = new BufferedReader(
            new InputStreamReader(clientSocket.getInputStream()));
        ) {

            String inputLine;

            if ((inputLine = in.readLine()) != null) {
                try {
                    System.out.println(inputLine);
                    JSONObject jsonIn = new JSONObject(inputLine);
                    if (playing == false) {
                        if (jsonIn.getString("username").equals("")) {
                            JSONObject jsonOut = new JSONObject("{\n" +
                                                    "\"status\": \"error\",\n" +
                                                    "\"description\": \"wrong request\"}");
                            out.println(jsonOut);
                        } else if (!listPlayer.checkPlayerExisted(jsonIn.getString("username"))){
                            listPlayer.addPlayer(playerId, jsonIn.getString("username"), 0);
                            JSONObject jsonOut = new JSONObject("{\n" +
                                                    "\"status\": \"ok\",\n" +
                                                    "\"player_id\": " + playerId + "}");
                            playerId++;
                            out.println(jsonOut);
                            username = jsonIn.getString("username");
                            valid = true;
                        } else {
                            JSONObject jsonOut = new JSONObject("{\n" +
                                                    "\"status\": \"fail\",\n" +
                                                    "\"description\": \"user exists\"}");
                            out.println(jsonOut);
                        }
                    } else {
                        JSONObject jsonOut = new JSONObject("{\n" +
                                                "\"status\": \"fail\",\n" +
                                                "\"description\": \"please wait, game is currently running\"}");
                        out.println(jsonOut);
                    }
                    
                } catch (JSONException ex) {
                    
                    Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            
            while (valid) {
                if ((inputLine = in.readLine()) != null) {
                    try {
                        System.out.println(username + " : " + inputLine);
                        JSONObject jsonIn = new JSONObject(inputLine);
                        if (jsonIn.getString("method").equals("leave")) {
                            listPlayer.removePlayer(username);
                            JSONObject jsonOut = new JSONObject("{\n" +
                                            "\"status\": \"ok\"}");
                            out.println(jsonOut);
                            System.out.println(jsonOut);
                            System.exit(0);
                        } else {
                            JSONObject jsonOut = new JSONObject("{\n" +
                                            "\"status\": \"error\"\n," + 
                                            "\"description\": \"wrong request\"}");
                            out.println(jsonOut);
                            System.out.println(jsonOut);
                        }
                    } catch (JSONException ex) {
                        Logger.getLogger(ServerThread.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            }
        } catch (IOException ex) {
            Logger.getLogger(ServerThread.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        
    }
    
    //GETTER AND SETTER
    public Socket getClientSocket() {
        return clientSocket;
    }

    public void setClientSocket(Socket clientSocket) {
        this.clientSocket = clientSocket;
    }

    public int getPlayerId() {
        return playerId;
    }

    public void setPlayerId(int playerId) {
        this.playerId = playerId;
    }

    public ListPlayer getListPlayer() {
        return listPlayer;
    }

    public void setListPlayer(ListPlayer listPlayer) {
        this.listPlayer = listPlayer;
    }

    public boolean isPlaying() {
        return playing;
    }

    public void setPlaying(boolean playing) {
        this.playing = playing;
    }
    
}

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
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 *
 * @author Candy
 */
public class ServerThread extends Thread {
    private Socket clientSocket = null;
    private int playerId;
    private ListPlayer listPlayer = new ListPlayer();
    private Player player = null;
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
                            player = listPlayer.addPlayer(playerId, jsonIn.getString("username"));
                            JSONObject jsonOut = new JSONObject("{\n" +
                                                    "\"status\": \"ok\",\n" +
                                                    "\"player_id\": " + playerId + "}");
                            //playerId++;
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
                            //System.exit(0);
                        }
                        else if (jsonIn.getString("method").equals("ready")) {
                            player.setReady(true);
                            JSONObject jsonOut = new JSONObject("{\n" +
                                            "\"status\": \"ok\",\n" +
                                            "\"description\": \"waiting for other player to start\"}");
                            out.println(jsonOut);
                            System.out.println(jsonOut);
                            
                            Thread thread = new Thread(){
                                public void run(){
                                    while (!checkAllReady()){
                                        try {
                                            System.out.println("not ready");
                                            sleep(1000);
                                        } catch (InterruptedException ex) {
                                            Logger.getLogger(ServerThread.class.getName()).log(Level.SEVERE, null, ex);
                                        }
                                    }
                                    // send start command
                                    listPlayer.randomRole();
                                    JSONObject start = startMessage(player);
                                    out.println(start);
                                    System.out.println(start);
                                }
                            };

                            thread.start();
                            
                        }
                        else if (jsonIn.getString("method").equals("client_address")) {
                            JSONObject jsonOut = new JSONObject("{\n" +
                                            "\"status\": \"ok\",\n" +
                                            "\"description\": \"list of clients retrieved\"}");
                            JSONArray clients = new JSONArray();
                            for(int i=0; i < listPlayer.getSize(); i++){
                                JSONObject player = new JSONObject();
                                player.put("player_id", listPlayer.getPlayer(i).getId());
                                player.put("is_alive", listPlayer.getPlayer(i).getAlive());
                                player.put("address", listPlayer.getPlayer(i).getAddress());
                                player.put("port", listPlayer.getPlayer(i).getPort());
                                player.put("username", listPlayer.getPlayer(i).getUsername());
                                if (listPlayer.getPlayer(i).getAlive() == 0)
                                    player.put("role", listPlayer.getPlayer(i).getRole());

                                clients.put(player);
                            }
                            
                            jsonOut.put("clients",clients);
                            out.println(jsonOut);
                            System.out.println(jsonOut);
                        }
                        
                        else {
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
    
    private boolean checkAllReady(){
        boolean allready = true;
        for(int i=0; i < listPlayer.getSize(); i++){
            if (!listPlayer.getPlayer(i).isReady()){
                allready = false;
                break;
            }   
        }
        return allready && (listPlayer.getSize() >= 3);
    }
    
    private JSONObject startMessage(Player player){
        try {
            JSONObject msg = new JSONObject();
            msg.put("method", "start");
            msg.put("time", "day");
            msg.put("role", player.getRole());
            if (player.getRole().equals("werewolf")){
                msg.put("friend", listPlayer.getWerewolfs());
            }
            msg.put("description", "game is started");
            
            return msg;
        } catch (JSONException ex) {
            Logger.getLogger(ServerThread.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
    }
    
    private JSONObject changePhase(String time, int days){
        try {
            JSONObject msg = new JSONObject();
            msg.put("method", "change_phase");
            msg.put("time", time);
            msg.put("days", days);
            msg.put("description", "");
            
            return msg;
        } catch (JSONException ex) {
            Logger.getLogger(ServerThread.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
    }
    
    private JSONObject voteNow(String phase){
        try {
            JSONObject msg = new JSONObject();
            msg.put("method", "vote_now");
            msg.put("phase", phase);
            return msg;
        } catch (JSONException ex) {
            Logger.getLogger(ServerThread.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
    }
    
    private JSONObject gameOver(String winner){
        try {
            JSONObject msg = new JSONObject();
            msg.put("method", "game_over");
            msg.put("winner", winner);
            msg.put("description", "");
            
            return msg;
        } catch (JSONException ex) {
            Logger.getLogger(ServerThread.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
    }
    
    
    
    
    
}
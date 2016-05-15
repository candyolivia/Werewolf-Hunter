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
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 *
 * @author Candy
 */
public class ServerThread implements Runnable {
    private static AtomicInteger statusPlayer;
    private Socket clientSocket = null;
    private int playerId;
    private ListPlayer listPlayer;
    private Player player = null;
    private boolean playing = false;
    private int pemilihanDay = 0;
    
    private String username;

    public ServerThread(Socket socket, ListPlayer _listPlayer) {
        this.clientSocket = socket;
        listPlayer = _listPlayer;
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
                            JSONObject jsonOut = new JSONObject();
                            jsonOut.put("status","error");
                            jsonOut.put("description", "wrong request");
                            out.println(jsonOut);
                        } else if (!listPlayer.checkPlayerExisted(jsonIn.getString("username"))){
                            player = listPlayer.addPlayer(playerId, jsonIn.getString("username"),jsonIn.getString("udp_address"),
                                    jsonIn.getInt("udp_port"), clientSocket);
                            JSONObject jsonOut = new JSONObject();
                            jsonOut.put("status","ok");
                            jsonOut.put("player_id", playerId);
                            out.println(jsonOut);
                            username = jsonIn.getString("username");
                            valid = true;
                        } else {
                            JSONObject jsonOut = new JSONObject();
                            jsonOut.put("status", "fail");
                            jsonOut.put("description","user exists");
                            out.println(jsonOut);
                        }
                    } else {
                        JSONObject jsonOut = new JSONObject();
                        jsonOut.put("status", "fail");
                        jsonOut.put("description", "please wait, game is currently running");
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
                        if (jsonIn.has("method")) {
                            if (jsonIn.getString("method").equals("leave")) {
                                listPlayer.removePlayer(username);
                                JSONObject jsonOut = new JSONObject();
                                jsonOut.put("status","ok");
                                out.println(jsonOut);
                                System.out.println(jsonOut);
                                //System.exit(0);
                            }
                            else if (jsonIn.getString("method").equals("ready")) {
                                player.setReady(true);
                                JSONObject jsonOut = new JSONObject();
                                jsonOut.put("status","ok");
                                jsonOut.put("description", "waiting for other player to start");
                                out.println(jsonOut);
                                System.out.println(jsonOut);

                                Thread thread = new Thread(){
                                    public void run(){
                                        while (!checkAllReady()){
                                            try {
                                                //System.out.println("not ready");
                                                sleep(1000);
                                            } catch (InterruptedException ex) {
                                                Logger.getLogger(ServerThread.class.getName()).log(Level.SEVERE, null, ex);
                                            }
                                        }
                                        // send start command
                                        //listPlayer.randomRole();
                                        JSONObject start = startMessage(player);
                                        out.println(start);
                                        System.out.println(start);
                                    }
                                };

                                thread.start();

                            }
                            else if (jsonIn.getString("method").equals("client_address")) {
                                JSONObject jsonOut = new JSONObject();
                                jsonOut.put("status", "ok");
                                jsonOut.put("description", "list of clients retrieved");
                                JSONArray clients = new JSONArray();
                                for(int i=0; i < listPlayer.getSize(); i++){
                                    JSONObject player = new JSONObject();
                                    player.put("player_id", listPlayer.getPlayer(i).getId());
                                    player.put("is_alive", listPlayer.getPlayer(i).getAlive());
                                    player.put("address", listPlayer.getPlayer(i).getAddress());
                                    player.put("port", listPlayer.getPlayer(i).getPort());
                                    player.put("username", listPlayer.getPlayer(i).getUsername());
                                    if (listPlayer.getPlayer(i).getRole().equals("werewolf"))
                                        player.put("role", listPlayer.getPlayer(i).getRole());

                                    clients.put(player);
                                }

                                jsonOut.put("clients",clients);
                                out.println(jsonOut);
                                System.out.println(jsonOut);
                            }
                            else if (jsonIn.getString("method").equals("accepted_proposal") && jsonIn.getString("description").equals("Kpu is selected")) {
                                JSONObject jsonOut = new JSONObject();
                                jsonOut.put("status","ok");
                                jsonOut.put("description","get KPU");
                                out.println(jsonOut);
                                System.out.println(jsonOut);

                                JSONObject kpuJSON = new JSONObject();
                                kpuJSON.put("method","kpu_selected");
                                kpuJSON.put("kpu_id", jsonIn.getInt("kpu_id"));
                                out.println(kpuJSON);
                                System.out.println(kpuJSON);

                            } 
                            else if (jsonIn.get("method").equals("vote_result_civilian")) {
                                if (jsonIn.get("vote_status").equals(1)) {
                                    int playerTerbunuh = jsonIn.getInt("player_killed");
                                    listPlayer.getPlayer(playerTerbunuh).setAlive(0);
                                    boolean gameberakhir = listPlayer.isGameOver();
                                    for (int i = 0; i < listPlayer.getPlayers().size();i++) {
                                        PrintWriter outSock =
                                            new PrintWriter(listPlayer.getPlayer(i).getSocket().getOutputStream(), true);
                                        if(!gameberakhir){
                                            outSock.println(changePhase("night",listPlayer.day));
                                            listPlayer.isDay = false;
                                        } else {
                                            if(listPlayer.isWerewolfWinner) outSock.println(gameOver("werewolf"));
                                            else outSock.println(gameOver("civilian"));
                                        }
                                    }
                                } else if (jsonIn.get("vote_status").equals(-1)) {
                                    pemilihanDay++;
                                    for (int i = 0; i < listPlayer.getPlayers().size();i++) {
                                        PrintWriter outSock =
                                            new PrintWriter(listPlayer.getPlayer(i).getSocket().getOutputStream(), true);
                                        if(pemilihanDay < 2) {
                                            System.err.println("jancuk11");
                                            outSock.println(voteNow("day"));
                                        } else {
                                            System.err.println("jancuk12");
                                            outSock.println(changePhase("night",listPlayer.day));
                                            listPlayer.isDay = false;
                                        }
                                    }
                                    if(pemilihanDay == 2) pemilihanDay = 0;
                                }
                            }
                            
                            else if (jsonIn.get("method").equals("vote_result_werewolf")) {
                                if (jsonIn.get("vote_status").equals(1)) {
                                    int playerTerbunuh = jsonIn.getInt("player_killed");
                                    listPlayer.getPlayer(playerTerbunuh).setAlive(0);
                                    boolean gameberakhir = listPlayer.isGameOver();
                                    listPlayer.day += 1;
                                    for (int i = 0; i < listPlayer.getPlayers().size();i++) {
                                        PrintWriter outSock =
                                            new PrintWriter(listPlayer.getPlayer(i).getSocket().getOutputStream(), true);
                                        if(!gameberakhir){
                                            outSock.println(changePhase("day",listPlayer.day));
                                            listPlayer.isDay = true;
                                        } else {
                                            if(listPlayer.isWerewolfWinner) outSock.println(gameOver("werewolf"));
                                            else outSock.println(gameOver("civilian"));
                                        }
                                    }
                                } else if (jsonIn.get("vote_status").equals(-1)) {
                                    for (int i = 0; i < listPlayer.getPlayers().size();i++) {
                                        PrintWriter outSock =
                                            new PrintWriter(listPlayer.getPlayer(i).getSocket().getOutputStream(), true);
                                        outSock.println(voteNow("night"));
                                    }
                                }
                            }
                        }
                        else {
                            if (jsonIn.getString("status").equals("ok")&&(jsonIn.getString("description").equals("ready to vote"))) {
                                listPlayer.setStatusPlayer(listPlayer.getStatusPlayer()+1);
                                System.out.println("jumlah status player : " + listPlayer.getStatusPlayer());
                                while (listPlayer.getStatusPlayer() < listPlayer.getPlayersAlive()) {
                                    try {
                                        Thread.sleep(1000);
                                    } catch (InterruptedException ex) {
                                        Logger.getLogger(ServerThread.class.getName()).log(Level.SEVERE, null, ex);
                                    }
                                }
                                if(listPlayer.isDay) out.println(voteNow("day"));
                                else out.println(voteNow("night"));
                                //1listPlayer.setStatusPlayer(0);
                            }
                            else {
                                JSONObject jsonOut = new JSONObject();
                                jsonOut.put("status","error");
                                jsonOut.put("description","wrong request");
                                out.println(jsonOut);
                                System.out.println(jsonOut);
                            }
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
        return allready && (listPlayer.getSize() >= 2);
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
            listPlayer.setStatusPlayer(0);
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
            System.out.println(msg);
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
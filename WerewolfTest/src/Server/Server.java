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
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.json.JSONException;
import org.json.JSONObject;

/**
 *
 * @author Candy
 */
public class Server {
    private static boolean checkAllReady(ListPlayer listPlayer){
        boolean allready = true;
        for(int i=0; i < listPlayer.getSize(); i++){
            if (!listPlayer.getPlayer(i).isReady()){
                allready = false;
                break;
            }   
        }
        return allready;
    }
    
    public static void main(String[] args) throws IOException {
        
        if (args.length != 1) {
            System.err.println("Usage: java LocalHost <port number>");
            System.exit(1);
        }

        int portNumber = Integer.parseInt(args[0]);
        
        int playerId = 0;
        Thread userReceiver;
        ListPlayer listPlayer = new ListPlayer();
        int numPlayers = 6;
        
        
        boolean listening = true;
         
        try (ServerSocket serverSocket = new ServerSocket(portNumber)) { 
            while (listening) { 
                ServerThread newServer = new ServerThread(serverSocket.accept(), listPlayer);
                userReceiver = new Thread(newServer);
                if (!listPlayer.isEmpty()){
                    playerId = listPlayer.getLastId() + 1;
                }
                newServer.setPlayerId(playerId);
                userReceiver.start();
            }
        } catch (IOException e) {
            System.err.println("Could not listen on port " + portNumber);
            System.exit(-1);
        }
    }    
}

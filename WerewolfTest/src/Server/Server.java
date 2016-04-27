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
    public static void main(String[] args) throws IOException {
        
        if (args.length != 1) {
            System.err.println("Usage: java KnockKnockServer <port number>");
            System.exit(1);
        }

        int portNumber = Integer.parseInt(args[0]);
        
        int playerId = 1;
        ListPlayer listPlayer = new ListPlayer();
        int numPlayers = 3;
        
        boolean listening = true;
        boolean cek = true;
         
        try (ServerSocket serverSocket = new ServerSocket(portNumber)) { 
            while (listening) {                              
                ServerThread newServer = new ServerThread(serverSocket.accept());
                newServer.setPlayerId(playerId);
                newServer.setListPlayer(listPlayer);
                if (playerId > numPlayers) {
                    newServer.setPlaying(true);
                } else {
                    newServer.setPlaying(false);
                }
                newServer.start();
                listPlayer = newServer.getListPlayer();
                listPlayer.print();
                //System.out.println(listPlayer.getSize()+1 + " " + playerId);
                if (listPlayer.getSize()+1 < playerId) {
                    
                } else {
                    playerId++;
                }
                
                
            }
        } catch (IOException e) {
            System.err.println("Could not listen on port " + portNumber);
            System.exit(-1);
        }
    }    
}
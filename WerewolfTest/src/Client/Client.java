/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
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
    private boolean isWerewolf;
    private Acceptor acceptor;
    
    public Client(){
        
    }
    
    public static void main(String[] args) throws IOException{
        String inputHostname = (String)JOptionPane.showInputDialog(
                                new JFrame(),
                                "Enter Host Name:\n",
                                "Enter Host Name",
                                JOptionPane.QUESTION_MESSAGE);
        
        String inputPortNumber = (String)JOptionPane.showInputDialog(
                                new JFrame(),
                                "Enter Port Number:\n",
                                "Enter Port Number",
                                JOptionPane.QUESTION_MESSAGE);
        
        
        String hostName = inputHostname;
        int portNumber = Integer.parseInt(inputPortNumber);
        boolean valid = false;

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
            
            while (inputUsername != null) {
                try {
                    JSONObject obj = new JSONObject();
                    obj.put("method", "join");
                    obj.put("username",inputUsername);
                    System.out.println("Client: " + obj);
                    out.println(obj);
                    
                    fromServer = in.readLine();
                    
                    JSONObject serverJSON = new JSONObject(fromServer);
                    
                    if (fromServer != null) {
                        System.out.println("Server: " + serverJSON);
                        if (serverJSON.get("status").equals("ok")){
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

                if (fromServer != null) {
                    System.out.println("Server: " + serverJSON);
                    if (serverJSON.has("method")){
                        switch(serverJSON.getString("method")){
                            case "": break;
                            case "start":
                                //olah status
                                JSONObject msg = new JSONObject();
                                msg.put("method", "client_address");
                                System.out.println("Client: " + msg);
                                out.println(msg);
                                JSONObject response = getResponse(in);

                                game.updatePlayerList(getUsernames(response));
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
    
    private static ArrayList getUsernames(JSONObject response){
        System.out.println(response);
        try {
            ArrayList<String> usernames = new ArrayList<String>();
            JSONArray client = response.getJSONArray("clients");
            for (int i=0; i < client.length(); i++){
                usernames.add(client.getJSONObject(i).getString("username"));
            }
            return usernames;
        } catch (JSONException ex) {
            Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
        
            return null;
        }
    }
    
}

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
    public static void main(String[] args) throws IOException{
        
        /* if (args.length != 2) {
            System.err.println(
                "Usage: java EchoClient <host name> <port number>");
            System.exit(1);
        }*/
        
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
            //System.out.print("Masukkan username : ");
            
            //fromUser = stdIn.readLine();
            while (inputUsername != null) {
                try {
                    JSONObject obj = new JSONObject("{\n" +
                            "\"method\": \"join\",\n" +
                            "\"username\": \""+inputUsername+"\"\n}");
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
            if (valid){
                java.awt.EventQueue.invokeLater(new Runnable() {
                public void run() {
                    GameView game = new GameView();
                    game.setVisible(true);
                    game.setOut(out);
                    game.setIn(in);
            }
        });
            }
            while (valid) {
                System.out.print("Masukkan method : ");

                fromUser = stdIn.readLine();
                if (fromUser.equals("leave")) {
                    JSONObject obj;
                    try {
                        obj = new JSONObject("{\n" +
                            "\"method\": \"leave\"\n}");
                        System.out.println("Client: " + obj);
                        out.println(obj);
                        
                        fromServer = in.readLine();

                        JSONObject serverJSON = new JSONObject(fromServer);

                        if (fromServer != null) {
                            System.out.println("Server: " + serverJSON);
                            if (serverJSON.getString("status").equals("ok")) {
                                System.out.println("Goodbye");
                                System.exit(0);
                            }
                        }
                    } catch (JSONException ex) {
                        Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
                    }

                } else {
                    JSONObject obj = new JSONObject("{\n" +
                            "\"method\": \""+fromUser+"\"\n}");
                        System.out.println("Client: " + obj);
                        out.println(obj);
                    
                    fromServer = in.readLine();

                    JSONObject serverJSON = new JSONObject(fromServer);

                    if (fromServer != null) {
                        System.out.println("Server: " + serverJSON);
                    }
                }
            }
            
//            while ((fromServer = in.readLine()) != null) {
//                System.out.println("Server: " + fromServer);
//                if (fromServer.equals("Bye."))
//                    break;
//                
//                fromUser = stdIn.readLine();
//                if (fromUser != null) {
//                    System.out.println("Client: " + fromUser);
//                    out.println(fromUser);
//                }
//            }
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
    
}

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Client;

import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author asus
 */
public class ToClientFromClient {

    private Proposer sender;
    private Thread senderth;
    private DatagramSocket socket;
    
    public ToClientFromClient(){
        try {
            socket = new DatagramSocket();
            sender = new Proposer(socket);
            senderth = new Thread(sender);
            senderth.start();
        } catch (SocketException ex) {
            Logger.getLogger(ToClientFromClient.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        // TODO code application logic here
        
        ToClientFromClient wew = new ToClientFromClient();
    }
    
}

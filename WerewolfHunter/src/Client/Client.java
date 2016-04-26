/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Client;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author asus
 */
public class Client {
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        // TODO code application logic here
        if(args.length < 2){
            System.err.println("How to Use:\n\tjava Client <hostname> <port>\n");
            System.exit(1);
        }
        String hostname = args[0];
        int port = Integer.parseInt(args[1]);
        try {
            BlockingQueue<String> bque = (BlockingQueue<String>) new LinkedBlockingQueue<String>();
            Socket sock = new Socket(hostname,port);
            ToClientFromServer rev = new ToClientFromServer(sock, bque);
            sendMessage send = new sendMessage(sock);
            Thread th = new Thread(rev);
            Thread thsend = new Thread(send);
            th.start();
            thsend.start();
            while(true){
                String message = (String) bque.take();
                System.out.println(message);
            }
        } catch (IOException ex) {
            Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
        } catch (InterruptedException ex) {
            Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
}

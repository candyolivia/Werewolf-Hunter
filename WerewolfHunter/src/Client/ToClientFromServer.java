/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Client;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.Socket;
import java.util.concurrent.BlockingQueue;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author asus
 */
public class ToClientFromServer implements Runnable {
    BlockingQueue<String> bque;
    Socket sock;

    public ToClientFromServer(Socket _sock, BlockingQueue<String> _bque) {
        this.sock = _sock;
        this.bque = _bque;
    }
    
    @Override
    public void run() {
        String message;
        try {
            ObjectInputStream in = new ObjectInputStream(sock.getInputStream());
            while((message = (String) in.readObject()) != null){
                bque.put(message);
            }
        } catch (IOException ex) {
            Logger.getLogger(ToClientFromServer.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(ToClientFromServer.class.getName()).log(Level.SEVERE, null, ex);
        } catch (InterruptedException ex) {
            Logger.getLogger(ToClientFromServer.class.getName()).log(Level.SEVERE, null, ex);
        }
        
    }
    
}

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Server;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.concurrent.BlockingQueue;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author asus
 */
public class UserReceiver implements Runnable {

    String userName;
    BlockingQueue<String> bque;
    ObjectInputStream ois;
    
    UserReceiver(String _userName, BlockingQueue _bque, ObjectInputStream _ois){
        this.userName = _userName;
        this.bque = _bque;
        this.ois = _ois;
    }
    
    @Override
    public void run() {
        String message;
        try {
            while((message = (String) ois.readObject()) != null){
                bque.put(message);
            }
        } catch (IOException ex) {
            Logger.getLogger(UserReceiver.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(UserReceiver.class.getName()).log(Level.SEVERE, null, ex);
        } catch (InterruptedException ex) {
            Logger.getLogger(UserReceiver.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
}

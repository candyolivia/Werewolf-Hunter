/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Server;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author asus
 */
public class UserRegister implements Runnable {
    Map<String, User> userMap;
    ServerSocket sock;
    BlockingQueue<String> bque;
    private sendMessage sendMsg;
    private Thread thread;

    public UserRegister(ServerSocket sock, Map<String, User> userMap, BlockingQueue<String> bque) {
        this.userMap = userMap;
        this.sock = sock;
        this.bque  = bque;
    }
    
    @Override
    public void run() {
        System.err.println("UserRegister started");
	Socket clientSock = null;
        try {
            while((clientSock = sock.accept()) != null){
                ObjectInputStream in = new ObjectInputStream(clientSock.getInputStream());
                String message = (String)in.readObject();
                bque.put(message);
                if (!userMap.containsKey("1")){
                        userMap.put("1", new User("1", clientSock, in, bque));
                        sendMsg = new sendMessage(userMap);
                        thread = new Thread(sendMsg);
                        thread.start();
                }
            }
        } catch (IOException ex) {
            Logger.getLogger(UserRegister.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(UserRegister.class.getName()).log(Level.SEVERE, null, ex);
        } catch (InterruptedException ex) {
            Logger.getLogger(UserRegister.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
}

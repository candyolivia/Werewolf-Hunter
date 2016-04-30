/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Server;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author asus
 */
public class Server {

    private Map<String, User> mapUser;
    private BlockingQueue<String> bque;
    private ServerSocket serverSock;
    private UserRegister userReg;
    private Thread userRegThread;
    
    public Server(int port) throws IOException{
        serverSock = new ServerSocket(port);
        bque = (BlockingQueue<String>) new LinkedBlockingQueue();
        mapUser = new HashMap<String, User>();
        userReg = new UserRegister(serverSock, mapUser, bque);
        userRegThread = new Thread(userReg);
    }
    
    public void Start(){
        System.out.println("Start");
        userRegThread.start();
        while(true){
            try {
                String message = (String) bque.take();
                System.out.println(message);
            } catch (InterruptedException ex) {
                Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        // TODO code application logic here
        if(args.length < 1) {
            System.err.println("How to Use:\n\tjava Server <port>\n");
            System.exit(1);
    	}
        int port = Integer.parseInt(args[0]);
        try {
            Server server = new Server(port);
            server.Start();
        } catch (IOException ex) {
            Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
        }
        
    }
    
}

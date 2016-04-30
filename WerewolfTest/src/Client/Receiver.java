/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Client;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author asus
 */
public class Receiver {
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws SocketException {
        // TODO code application logic here
        
        int port;
        DatagramSocket socket;
        DatagramPacket packet;
        InetAddress address;
        
        if(args.length < 1){
            System.err.println("use : java Receiver port");
            return;
        }
        
        byte[] buf = new byte[1024];
        packet = new DatagramPacket(buf, buf.length);
        socket = new DatagramSocket(Integer.parseInt(args[0]));
        while(true){
            try {
                socket.receive(packet);
                String received = new String(packet.getData(), 0, packet.getLength());
                System.out.println(received);
            } catch (SocketException ex) {
                Logger.getLogger(Receiver.class.getName()).log(Level.SEVERE, null, ex);
            } catch (IOException ex) {
                Logger.getLogger(Receiver.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
    
}

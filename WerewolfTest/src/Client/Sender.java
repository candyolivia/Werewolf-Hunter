/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author asus
 */
public class Sender implements Runnable{

    DatagramSocket socket;
    byte[] buf;
    DatagramPacket sendData;
    DatagramPacket receiveData;
    
    public Sender(DatagramSocket _socket){
        socket = _socket;
        buf = new byte[1024];
    }
    
    @Override
    public void run() {
        while(true){
            System.out.println("masukkan kata : ");
            BufferedReader inFromUser = new BufferedReader(new InputStreamReader(System.in));
            String sentence;
            try {
                sentence = inFromUser.readLine();
                buf = sentence.getBytes();
                InetAddress address = InetAddress.getByName("localhost");
                sendData = new DatagramPacket(buf, buf.length, address, 1234);
                socket.send(sendData);
            } catch (IOException ex) {
                Logger.getLogger(Sender.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
    
}

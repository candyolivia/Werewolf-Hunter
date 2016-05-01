/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Client;

import Server.ListPlayer;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.json.JSONException;
import org.json.JSONObject;

/**
 *
 * @author User
 */
public class KPU implements Runnable{
    DatagramSocket socket;
    byte[] buf;
    DatagramPacket sendData;
    DatagramPacket receiveData;
    private ListPlayer listPlayers;
    private int playerID;
    private boolean killWerewolf = false;
    private boolean killCivillian = false;
    private boolean firstRun = true;
    
    public KPU(DatagramSocket _socket, int _playerID, ListPlayer _listPlayer) throws JSONException {
        socket = _socket;
        playerID = _playerID;
        listPlayers = _listPlayer;
    }
    
    public void broadCastKPU() throws JSONException {
        JSONObject kpuSelected = new JSONObject();
        kpuSelected.put("method", "accept_proposal");
        String proposalID = "[0,"+(listPlayers.getSize()-1)+"]";
        kpuSelected.put("proposal_id", proposalID); //asumsi selalu yang [0.n-1]
        kpuSelected.put("kpu_id", (listPlayers.getSize()-1));

        for (int i = 0; i < listPlayers.getSize(); i++) {
            InetAddress address;
            try {
                address = InetAddress.getByName(listPlayers.getPlayer(i).getAddress());
                int port = listPlayers.getPlayer(i).getPort();
                buf = kpuSelected.toString().getBytes();
                sendData = new DatagramPacket(buf, buf.length, address,port);
                System.out.println("kpu send : " + new String(sendData.getData()) + "ke " + address + " : " + port);
                socket.send(sendData);
                
                socket.setSoTimeout(1000);
                
                buf = new byte[1024];
                receiveData = new DatagramPacket(buf, buf.length);
                System.out.println("ssss");
                
                while(true) {
                    try {
                        socket.receive(receiveData);
                        System.out.println("aaaa");
                        System.out.println("kpu receive : " + new String(receiveData.getData()));
                    } catch (SocketTimeoutException e) {
                        // timeout exception.
                        System.out.println("bbbb");
                        System.out.println("Timeout reached!!! " + e);
                        socket.send(sendData);
                    }
                }
            } catch (UnknownHostException ex) {
                Logger.getLogger(Acceptor.class.getName()).log(Level.SEVERE, null, ex);
            } catch (IOException ex) {
                Logger.getLogger(Acceptor.class.getName()).log(Level.SEVERE, null, ex);
            }

        }
    }

    @Override
    public void run() {
        while (firstRun) {
            try {
                broadCastKPU();
                firstRun = false;
                
            } catch (JSONException ex) {
                Logger.getLogger(KPU.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
    
}

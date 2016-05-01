/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Client;

import Server.ListPlayer;
import java.net.DatagramPacket;
import java.net.DatagramSocket;

/**
 *
 * @author User
 */
public class Leader {
    DatagramSocket socket;
    byte[] buf;
    DatagramPacket sendData;
    private ListPlayer listPlayers;
    private int playerID;
    private boolean killWerewolf = false;
    private boolean killCivillian = false;
    
    
}

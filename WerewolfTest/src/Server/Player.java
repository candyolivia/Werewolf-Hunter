/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Server;

import java.net.Socket;

/**
 *
 * @author Candy
 */
public class Player {
    private int id;
    private String username;
    private String role; //Werewolf (0) or Civillian(1)
    private String address;
    private int port;
    private int alive = 1;
    private boolean ready = false;
    private Socket socket;
    

    //CONSTRUCTOR
    public Player(int _id, String _username, String _role, String _address, int _port, Socket sock) {
        id = _id;
        username = _username;
        role = _role;
        address = _address;
        port = _port;
        socket = sock;
    }

    /**
     * @return the id
     */
    public int getId() {
        return id;
    }

    /**
     * @param id the id to set
     */
    public void setId(int id) {
        this.id = id;
    }

    /**
     * @return the username
     */
    public String getUsername() {
        return username;
    }

    /**
     * @param username the username to set
     */
    public void setUsername(String username) {
        this.username = username;
    }

    /**
     * @return the role
     */
    public String getRole() {
        return role;
    }

    /**
     * @param role the role to set
     */
    public void setRole(String role) {
        this.role = role;
    }

    /**
     * @return the address
     */
    public String getAddress() {
        return address;
    }

    /**
     * @param address the address to set
     */
    public void setAddress(String address) {
        this.address = address;
    }

    /**
     * @return the port
     */
    public int getPort() {
        return port;
    }

    /**
     * @param port the port to set
     */
    public void setPort(int port) {
        this.port = port;
    }

    /**
     * @return the alive
     */
    public int getAlive() {
        return alive;
    }

    /**
     * @param alive the alive to set
     */
    public void setAlive(int alive) {
        this.alive = alive;
    }

    /**
     * @return the ready
     */
    public boolean isReady() {
        return ready;
    }

    /**
     * @param ready the ready to set
     */
    public void setReady(boolean ready) {
        this.ready = ready;
    }

    public Socket getSocket() {
        return socket;
    }

    public void setSocket(Socket socket) {
        this.socket = socket;
    }
    
}
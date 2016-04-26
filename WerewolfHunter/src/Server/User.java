/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Server;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.Socket;
import java.util.concurrent.BlockingQueue;

/**
 *
 * @author asus
 */
public class User implements Serializable{
    private transient Socket _sock;
    private transient ObjectInputStream in;
    private transient ObjectOutputStream out;
    private String _name;
    private transient UserReceiver _receiver;
    private transient Thread thread;
    private boolean _isActive;
    
    public User(String _name, Socket sock, ObjectInputStream in, BlockingQueue bque) throws IOException{
        this._sock = sock;
        this._name = _name;
        this.in = in;
        this._isActive = true;
        this.out = new ObjectOutputStream(sock.getOutputStream());
        this._receiver = new UserReceiver(_name, bque, in);
        thread = new Thread(this.getReceiver());
        thread.start();
    }

    /**
     * @return the _sock
     */
    public Socket getSock() {
        return _sock;
    }

    /**
     * @param _sock the _sock to set
     */
    public void setSock(Socket _sock) {
        this._sock = _sock;
    }

    /**
     * @return the out
     */
    public ObjectOutputStream getOutput() {
        return out;
    }

    /**
     * @return the _name
     */
    public String getName() {
        return _name;
    }

    /**
     * @param _name the _name to set
     */
    public void setName(String _name) {
        this._name = _name;
    }

    /**
     * @return the _isActive
     */
    public boolean isIsActive() {
        return _isActive;
    }

    /**
     * @param _isActive the _isActive to set
     */
    public void setIsActive(boolean _isActive) {
        this._isActive = _isActive;
    }

    /**
     * @return the _receiver
     */
    public UserReceiver getReceiver() {
        return _receiver;
    }
}

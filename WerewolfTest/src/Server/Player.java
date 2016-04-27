/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Server;

/**
 *
 * @author Candy
 */
public class Player {
    private int id;
    private String username;
    private int role; //Werewolf (0) or Civillian(1)

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public int getRole() {
        return role;
    }

    public void setRole(int role) {
        this.role = role;
    }
    
    //CONSTRUCTOR
    public Player(int _id, String _username, int _role) {
        id = _id;
        username = _username;
        role = _role;
    }
}

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Server;

import java.util.ArrayList;
import java.util.Iterator;

/**
 *
 * @author Candy
 */
public class ListPlayer {
    private ArrayList<Player> players = new ArrayList<Player>();
    
    public ArrayList getPlayers() {
        return players;
    }

    public void setPlayers(ArrayList players) {
        this.players = players;
    }
    
    public void addPlayer(int id, String username, int role){
        Player newPlayer = new Player(id, username, role);
        players.add(newPlayer);
    }
    
    public void removePlayer(String username){
        Iterator<Player> it = players.iterator();
        while (it.hasNext()) {
          Player p = it.next();
          if (p.getUsername().equals(username)) {
            it.remove();
          }
        }
    }
    
    public boolean checkPlayerExisted(String username){
        boolean check = false;
        for (int i = 0; i < players.size(); i++) {
            if (players.get(i).getUsername().equals(username)){
                //System.out.println(players.get(i).getUsername()+ " " +username);
                check = true;
                break;
            }
        }
        return check;
    }
    
    public void print(){
        for (int i = 0; i < players.size(); i++) {
            System.out.println(players.get(i).getId() + " " + players.get(i).getUsername() + " " + players.get(i).getRole());
        }
    }
    
    public int getSize(){
        return players.size();
    }
}
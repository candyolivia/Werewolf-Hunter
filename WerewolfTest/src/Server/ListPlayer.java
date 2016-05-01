/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Server;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 *
 * @author Candy
 */
public class ListPlayer {
    private static ArrayList<Player> players = new ArrayList<Player>();
    
    public ArrayList getPlayers() {
        return players;
    }
    
    public Player getPlayer(int i){
        return players.get(i);
    }
    
    public void setPlayers(ArrayList players) {
        this.players = players;
    }
    
    public Player addPlayer(int id, String username){
        Player newPlayer = new Player(id, username, "civilian");
        players.add(newPlayer);
        
        return newPlayer;
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
    
    
    public boolean checkPlayerIdExisted(int id){
        boolean check = false;
        for (int i = 0; i < players.size(); i++) {
            if (players.get(i).getId() == id){
                //System.out.println(players.get(i).getUsername()+ " " +username);
                check = true;
                break;
            }
        }
        return check;
    }
    
    public void print(){
        for (int i = 0; i < players.size(); i++) {
            System.out.println(players.get(i).getId() + " " + players.get(i).getUsername() + " " + players.get(i).isReady());
        }
    }
    
    public int getSize(){
        return players.size();
    }
    
    public int getLastId(){
        return players.get(players.size()-1).getId();
    }
    
    public boolean isEmpty(){
        return players.isEmpty();
    }
    
    public void randomRole(){
        int random1 = randomNum(0,getLastId());
        players.get(random1).setRole("werewolf");
        int random2 = randomNum(0,getLastId());
        while (random1 == random2){
            random2 = randomNum(0,getLastId());
        }
        players.get(random2).setRole("werewolf");
    }
    
    private int randomNum(int min, int max){
        Random rand = new Random();
        int randomNum = rand.nextInt((max - min) + 1) + min;
        return randomNum;
    }
    
    public JSONArray getWerewolfs(){
        JSONArray werewolfs = new JSONArray();
        for (int i = 0; i < players.size(); i++) {
            if (getPlayer(i).getRole().equals("werewolf")){
                    werewolfs.put(getPlayer(i).getUsername());
            }
        }
        
        return werewolfs;
    }
}

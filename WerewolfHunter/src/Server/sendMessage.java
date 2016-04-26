/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Server;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.Map;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author asus
 */
public class sendMessage implements Runnable{
    
    private ObjectOutputStream out;
    private Map<String, User> mapUser;
    Scanner in = new Scanner(System.in);

    public sendMessage(Map<String, User> _mapUser) {
        this.mapUser = _mapUser;
    }
    
    @Override
    public void run() {
        while(!mapUser.containsKey("1"));
        User user = mapUser.get("1");
        this.out = user.getOutput();
        while(true){
            try {
                System.out.println("Masukkan string yang dikirim : ");
                String s = in.nextLine();
                out.writeObject(s);
            } catch (IOException ex) {
                Logger.getLogger(sendMessage.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
}

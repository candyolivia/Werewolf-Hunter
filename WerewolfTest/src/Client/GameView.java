/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.json.JSONException;
import org.json.JSONObject;

/**
 *
 * @author jessica
 */
public class GameView extends javax.swing.JFrame {

    /**
     * Creates new form GameView
     */
    private PrintWriter out;
    private BufferedReader in;
    private boolean isReadyButtonClicked = false;
    
    public GameView() {
        initComponents();
        jPanel1.setVisible(false);
        jScrollPane1.setVisible(false);
        playerList.setVisible(false);
        leaveButton.setVisible(false);
        voteButton.setVisible(false);
        this.revalidate();
        
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        readyPane = new javax.swing.JPanel();
        readyButton = new javax.swing.JButton();
        leaveButton = new javax.swing.JButton();
        voteButton = new javax.swing.JButton();
        jScrollPane1 = new javax.swing.JScrollPane();
        playerList = new javax.swing.JTextArea();

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 100, Short.MAX_VALUE)
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 100, Short.MAX_VALUE)
        );

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("Werewolf Hunter");
        setBackground(new java.awt.Color(153, 204, 255));
        setMaximumSize(new java.awt.Dimension(1366, 768));

        readyButton.setText("Ready");
        readyButton.setPreferredSize(new java.awt.Dimension(70, 70));
        readyButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                readyButtonActionPerformed(evt);
            }
        });

        leaveButton.setText("Leave");
        leaveButton.setPreferredSize(new java.awt.Dimension(70, 70));
        leaveButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                leaveButtonActionPerformed(evt);
            }
        });

        voteButton.setText("Vote!");
        voteButton.setPreferredSize(new java.awt.Dimension(70, 70));

        jScrollPane1.setPreferredSize(new java.awt.Dimension(146, 96));

        playerList.setEditable(false);
        playerList.setColumns(20);
        playerList.setRows(5);
        playerList.setText("Waiting for other player to start...");
        playerList.setWrapStyleWord(true);
        playerList.setPreferredSize(new java.awt.Dimension(150, 94));
        jScrollPane1.setViewportView(playerList);

        javax.swing.GroupLayout readyPaneLayout = new javax.swing.GroupLayout(readyPane);
        readyPane.setLayout(readyPaneLayout);
        readyPaneLayout.setHorizontalGroup(
            readyPaneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(readyPaneLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(readyButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 376, Short.MAX_VALUE)
                .addGroup(readyPaneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addGroup(readyPaneLayout.createSequentialGroup()
                        .addComponent(voteButton, javax.swing.GroupLayout.PREFERRED_SIZE, 70, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(leaveButton, javax.swing.GroupLayout.PREFERRED_SIZE, 70, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        readyPaneLayout.setVerticalGroup(
            readyPaneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(readyPaneLayout.createSequentialGroup()
                .addGroup(readyPaneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(readyPaneLayout.createSequentialGroup()
                        .addGap(21, 21, 21)
                        .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 367, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(readyPaneLayout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(readyButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(readyPaneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(leaveButton, javax.swing.GroupLayout.PREFERRED_SIZE, 70, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(voteButton, javax.swing.GroupLayout.PREFERRED_SIZE, 70, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(readyPane, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(readyPane, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
        );

        pack();
        setLocationRelativeTo(null);
    }// </editor-fold>//GEN-END:initComponents

    private void readyButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_readyButtonActionPerformed
        // TODO add your handling code here:
        JSONObject obj;
        try {
            obj = new JSONObject();
            obj.put("method", "ready");
            System.out.println("Client: " + obj);
            out.println(obj);
            
            /*String fromServer = in.readLine();

            JSONObject serverJSON = new JSONObject(fromServer);

            if (fromServer != null) {
                System.out.println("Server: " + serverJSON);
            }
            
            if (serverJSON.getString("status").equals("ok")){
                readyButton.setVisible(false);
                jScrollPane1.setVisible(true);
                playerList.setVisible(true);
                playerList.setLineWrap(true);
                leaveButton.setVisible(true);
                voteButton.setVisible(true);
                //leaveButton.setEnabled(false);
                //voteButton.setEnabled(false);
                this.revalidate();
            }
            */
        }
        catch (JSONException ex) {
                    Logger.getLogger(GameView.class.getName()).log(Level.SEVERE, null, ex);
                }
        
                readyButton.setVisible(false);
                jScrollPane1.setVisible(true);
                playerList.setVisible(true);
                playerList.setLineWrap(true);
                leaveButton.setVisible(true);
                voteButton.setVisible(true);
                //leaveButton.setEnabled(false);
                //voteButton.setEnabled(false);
                this.revalidate();
        
        //waitForStart();
    }//GEN-LAST:event_readyButtonActionPerformed

    private void leaveButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_leaveButtonActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_leaveButtonActionPerformed

    public synchronized void updatePlayerList(ArrayList list){
        String players ="";
        for(int i=0; i<list.size(); i++){
            players += (list.get(i) + "\n");
        }
        playerList.setText(players);
        
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel jPanel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JButton leaveButton;
    private javax.swing.JTextArea playerList;
    private javax.swing.JButton readyButton;
    private javax.swing.JPanel readyPane;
    private javax.swing.JButton voteButton;
    // End of variables declaration//GEN-END:variables

    /**
     * @return the out
     */
    public PrintWriter getOut() {
        return out;
    }

    /**
     * @param out the out to set
     */
    public void setOut(PrintWriter out) {
        this.out = out;
    }

    /**
     * @return the in
     */
    public BufferedReader getIn() {
        return in;
    }

    /**
     * @param in the in to set
     */
    public void setIn(BufferedReader in) {
        this.in = in;
    }
}

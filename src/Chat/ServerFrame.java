package Chat;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;


public class ServerFrame extends javax.swing.JFrame {

    final int SERVER_PORT = 18524;//Which port the server listening to
    Server myServer;
  
    public ServerFrame() {
        initComponents();
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        btnStart = new javax.swing.JButton();
        jScrollPane1 = new javax.swing.JScrollPane();
        txtLog = new javax.swing.JTextArea();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        btnStart.setText("Start");
        btnStart.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnStartActionPerformed(evt);
            }
        });

        txtLog.setColumns(20);
        txtLog.setRows(5);
        jScrollPane1.setViewportView(txtLog);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 475, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(btnStart)
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(btnStart)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 320, Short.MAX_VALUE)
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

   
    private void btnStartActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnStartActionPerformed
        startStopServer();
    }//GEN-LAST:event_btnStartActionPerformed

    public static void main(String args[]) {
     
        java.awt.EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                new ServerFrame().setVisible(true);
            }
        });
    }

  
    private void startStopServer() {
        if (btnStart.getText().equals("Start")) {
            btnStart.setText("Stop");
            myServer = new Server();
            new Thread(myServer).start();
        } else {
            btnStart.setText("Start");
            if (myServer != null) {
                myServer.stopServer();
            }
        }
    }

    public class Server implements Runnable {

        //Hold the references to all the clients as threads
        private final ArrayList<ClientThread> connectedClients;
        //All the clients connect to this server socket
        private ServerSocket serverSocket;

        /**
         * Initializing the list of the clients.
         */
        public Server() {
            connectedClients = new ArrayList<>();
        }

        
        public void stopServer() {
            txtLog.append("Closed server socket.\n");
            sendMessageAllClient("<Server disconnected>");
            closeAllClients();
            connectedClients.clear();
            try {
                serverSocket.close();
            } catch (IOException ex) {
                Logger.getLogger(ServerFrame.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        private void closeAllClients() {
            synchronized (connectedClients) {
                for (ClientThread current : connectedClients) {
                    current.closeConnection();
                }
            }
        }

        public boolean isLegalName(String name) {
            synchronized (connectedClients) {
                if (name.equals("all")) {
                    return false;
                }
                for (ClientThread current : connectedClients) {
                    if (current.info.name.equals(name)) {
                        return false;
                    }
                }
                return true;
            }
        }

        
        public void sendMessageAllClient(String msg) {
            synchronized (connectedClients) {
                for (ClientThread current : connectedClients) {
                    current.out.println(msg);
                }
            }
     
        public synchronized boolean sendPrivateMsg(String senderName, String recipientName, String msg) {
            if (senderName.equals(recipientName)) {
                return false;
            }
            ClientThread recipient = getClientThread(recipientName);
            if (recipient == null) {
                return false;
            }
            recipient.out.println("<Private message from: " + senderName + "> " + msg);
            return true;
        }

      
        private ClientThread getClientThread(String clientsName) {
            synchronized (connectedClients) {
                for (ClientThread current : connectedClients) {
                    if (current.info.name.equals(clientsName)) {
                        return current;
                    }
                }
                return null;
            }
        }

        
        @Override
        public void run() {
            try {
                //Start listening to connections at the specified port number
                serverSocket = new ServerSocket(SERVER_PORT);
                txtLog.append("Chat server is up and running and listening on port " + SERVER_PORT + ".\n");
                while (true) {
                    Socket clientSocket = serverSocket.accept();
                    //Some client connected. start a new thread only for this client
                    if (btnStart.getText().equals("Stop")) {
                        ClientThread clientThread = new ClientThread(clientSocket);
                        clientThread.start();
                    } else {
                        return;
                    }
                }
            } catch (IOException e) {
                if (btnStart.getText().equals("Stop")) {
                    txtLog.setText("Error has been occured while starting the server. \n\tError: " + e.toString() + "\n");
                    btnStart.setText("Start");
                }
            }
        }

    
        public boolean isLegalName(String name, ArrayList<ClientThread> connectedClients) {
            synchronized (connectedClients) {
                if (name.equals("all")) {
                    return false;
                }
                for (ClientThread current : connectedClients) {
                    if (current.info.name.equals(name)) {
                        return false;
                    }
                }
                return true;
            }
        }
        
        public class ClientThread extends Thread {
            /**
             * The socket which is the connection between the server and the client
             */
            public Socket curClient; 
            /**
             * Stream to write data to the client
             */
            public PrintWriter out; //
            /**
             * Stream to read data from the client
             */
            public BufferedReader in; 
            /**
             * The class that will be restored from a string which is the serialized.
             * same class containing all the needed data from the client
             */
            ClientInfoSeirialized info;
          
            public ClientThread(Socket curClient) {
                this.curClient = curClient;
                info = new ClientInfoSeirialized();
            }
       
             public ClientThread(Socket curClient, String name) {
                this.curClient = curClient;
                info = new ClientInfoSeirialized();
                info.name = name;
            }

            @Override
            public void run() {
                try {
                    //Socket output to the client - for sending data through the socket to the client
                    out = new PrintWriter(curClient.getOutputStream(), true);
                    //Socket input from cleint - for reading clent's data
                    in = new BufferedReader(new InputStreamReader(curClient.getInputStream()));
                    //Start listening to messages from the client

                    //First meesage is only the name of the client
                    String name = in.readLine();
                    if (isLegalName(name)) {
                        txtLog.append("Client " + name + " connected.\n");
                        sendMessageAllClient("<Client " + name + " has entered>");
                        out.println("Welcome, " + name);
                        //Add to the list of all connected clients
                        connectedClients.add(this);

                        info.name = name;

                        //Send or 'who's online' was clicked so now there is data in the InputStream
                        String serializedFromClient;
                        while ((serializedFromClient = in.readLine()) != null) {
                            try {
                                System.out.println(serializedFromClient);
                                info = (ClientInfoSeirialized) SerializeDeserialize.fromString(serializedFromClient);
                                //who's online was clicked
                                if (info.showOnline) {
                                    out.println(getConnectedClients());
                                } 
                                //The user is sending a message to all the clients
                                else if (info.recipient.equals("all")) {
                                    sendMessageAllClient(info.name + ": " + info.msg);
                                } //The user wants to send a private message
                                else {
                                    //Failed sending the private message
                                    if (!sendPrivateMsg(info.name, info.recipient, info.msg)) {
                                        out.println("<Couldn't send your message to " + info.recipient + ">");
                                    } else { //Succeded sending the private message
                                        out.println("<Sent: " + info.msg + " Only to: " + info.recipient + ">");
                                    }
                                }
                            } catch (ClassNotFoundException e) {
                                System.out.println("Error in deserialization" + e.toString());
                            }
                        }
                        txtLog.append("Client " + info.name + " disconnected\n");
                        sendMessageAllClient("<Client " + info.name + " disconnected>");
                    } else {
                        out.println("<Connection rejected because your name is 'all' or your name is already taken>");
                    }
                } //Unexpectedly lost connection with the client
                catch (IOException e) {
                    if (btnStart.getText().equals("Stop")) {
                        txtLog.append("Lost connection with " + info.name + ".\n\tError: + " + e.toString() + "\n");
                        sendMessageAllClient("<Client " + info.name + " has been disconnected>");
                    }
                } finally {
                    synchronized (connectedClients) {
                        connectedClients.remove(this);
                    }
                    closeConnection();
                }
            }

          
            private String getConnectedClients() {
                synchronized (connectedClients) {
                    String allConnected = "";
                    allConnected = "<Now online:";
                    for (ClientThread current : connectedClients) {
                        allConnected += current.info.name + ", ";
                    }
                    return allConnected.substring(0, allConnected.length() - 2) + ">";
                }
            }

           
            public void closeConnection() {
                try {
                    if (out != null) {
                        out.close();
                    }
                    if (in != null) {
                        in.close();
                    }
                    if (curClient != null) {
                        curClient.close();
                    }
                } catch (IOException ex) {
                    Logger.getLogger(ServerFrame.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnStart;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTextArea txtLog;
    // End of variables declaration//GEN-END:variables
}

package DAO;

import java.io.*;
import java.net.*;
import java.sql.*;
import java.util.*;
import javax.swing.JOptionPane;
import view.ServerView;

public class ServerDAO{
    private static ServerSocket serverSocket;
    private static int PORT = 5000;
    private static Connection conn = connect.connectDb.getconnect();
    private static List<ClientHandler> clients = Collections.synchronizedList(new ArrayList<>());
    boolean keepGoing = true;
    private Thread serverThread;
    public void run(){
        try {
            serverSocket = new ServerSocket(PORT);
            System.out.println("Server is listening on port " + PORT);
            while (keepGoing) {
                Socket socket = serverSocket.accept();
                System.out.println("New client connected");
                ClientHandler clientHandler = new ClientHandler(socket);
                clients.add(clientHandler);
                clientHandler.start();
            }
        } catch (Exception e) {
        }
    }
    public void stop(){
      try {
     serverSocket.close();
      keepGoing = false;
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }
    public void start() {
        serverThread = new Thread(this::run);
        serverThread.start();
    }

    private static class ClientHandler extends Thread {
        private Socket socket;
        private PrintWriter out;
        private BufferedReader in;
        private int userId;
        private String username;

        public ClientHandler(Socket socket) {
            this.socket = socket;
        }

        public void run() {
            try {
                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                out = new PrintWriter(socket.getOutputStream(), true);

                username = in.readLine();
                userId = getUserId(username);

                sendExistingMessages();
                sendOnlineUsers();
                broadcastUserStatus(username, true);

                String message;
                while ((message = in.readLine()) != null) {
                    saveMessage(userId, message);
                    broadcastMessage("[" + username + "]: " + message);
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    clients.remove(this);
                    broadcastUserStatus(username, false);
                }
            }
        }

        private int getUserId(String username) {
            try {
                PreparedStatement stmt = conn.prepareStatement("SELECT id FROM Users WHERE username = ?");
                stmt.setString(1, username);
                ResultSet rs = stmt.executeQuery();

                if (rs.next()) {
                    return rs.getInt("id");
                } else {
                    stmt = conn.prepareStatement("INSERT INTO Users (username) VALUES (?)", Statement.RETURN_GENERATED_KEYS);
                    stmt.setString(1, username);
                    stmt.executeUpdate();

                    rs = stmt.getGeneratedKeys();
                    if (rs.next()) {
                        return rs.getInt(1);
                    }
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
            return -1;
        }

        private void saveMessage(int userId, String message) {
            try {
                PreparedStatement stmt = conn.prepareStatement("INSERT INTO Messages (sender_id, message) VALUES (?, ?)");
                stmt.setInt(1, userId);
                stmt.setString(2, message);
                stmt.executeUpdate();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        private void sendExistingMessages() {
            try {
                PreparedStatement stmt = conn.prepareStatement("SELECT u.username, m.message, m.timestamp FROM Messages m JOIN Users u ON m.sender_id = u.id ORDER BY m.timestamp");
                ResultSet rs = stmt.executeQuery();

                while (rs.next()) {
                    String sender = rs.getString("username");
                    String message = rs.getString("message");
                    Timestamp timestamp = rs.getTimestamp("timestamp");

                    out.println("[" + timestamp + "] " + sender + ": " + message);
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        private void sendOnlineUsers() {
            synchronized (clients) {
                out.println("ONLINE_USERS_START");
                for (ClientHandler client : clients) {
                    if (client != this) {
                        out.println(client.username);
                    }
                }
                out.println("ONLINE_USERS_END");
            }
        }

        private void broadcastUserStatus(String username, boolean isOnline) {
            synchronized (clients) {
                String statusMessage = isOnline ? "USER_ONLINE" : "USER_OFFLINE";
                for (ClientHandler client : clients) {
                    client.out.println(statusMessage + ":" + username);
                }
            }
        }

        private void broadcastMessage(String message) {
            synchronized (clients) {
                for (ClientHandler client : clients) {
                    client.out.println(message);
                }
            }
        }
   
    }
        

}

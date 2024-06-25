import java.io.*;
import java.net.*;
import java.sql.*;
import java.util.*;

public class Server {
    private static final int PORT = 12345;
    private static Connection conn;
    private static Set<ClientHandler> clientHandlers = new HashSet<>();

    public static void main(String[] args) {
        try {
            // Mở server socket để lắng nghe kết nối từ client
            ServerSocket serverSocket = new ServerSocket(PORT);
            System.out.println("Server is listening on port " + PORT);
            
            // Kết nối tới cơ sở dữ liệu MySQL
            conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/ChatApp", "root", "password");

            // Vòng lặp để chấp nhận kết nối từ nhiều client
            while (true) {
                Socket socket = serverSocket.accept(); // Chấp nhận kết nối từ client
                ClientHandler clientHandler = new ClientHandler(socket); // Tạo một handler cho client mới
                clientHandlers.add(clientHandler); // Thêm handler vào danh sách các client đang kết nối
                clientHandler.start(); // Bắt đầu xử lý luồng dữ liệu của client
            }
        } catch (IOException | SQLException e) {
            e.printStackTrace();
        }
    }

    // Class ClientHandler để xử lý dữ liệu từ client
    private static class ClientHandler extends Thread {
        private Socket socket;
        private PrintWriter out;
        private BufferedReader in;
        private String username;
        private int userId;

        public ClientHandler(Socket socket) {
            this.socket = socket;
        }

        public void run() {
            try {
                // Tạo luồng vào và ra để giao tiếp với client
                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                out = new PrintWriter(socket.getOutputStream(), true);

                // Đọc username từ client
                this.username = in.readLine();
                
                // Lấy hoặc tạo user mới từ cơ sở dữ liệu và lấy userId
                this.userId = getUserId(username);

                // Gửi các tin nhắn cũ tới client khi kết nối
                List<String> oldMessages = getOldMessages();
                for (String msg : oldMessages) {
                    out.println(msg);
                }

                String message;
                // Đọc và lưu các tin nhắn mới từ client, sau đó broadcast đến các client khác
                while ((message = in.readLine()) != null) {
                    saveMessage(userId, message);
                    broadcastMessage("[" + username + "]: " + message);
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    // Khi client ngắt kết nối, xóa handler khỏi danh sách và đóng socket
                    clientHandlers.remove(this);
                    socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        // Phương thức để lấy userId từ username trong cơ sở dữ liệu
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

        // Phương thức để lưu tin nhắn vào cơ sở dữ liệu
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

        // Phương thức để lấy các tin nhắn cũ từ cơ sở dữ liệu
        private List<String> getOldMessages() {
            List<String> messages = new ArrayList<>();
            try {
                PreparedStatement stmt = conn.prepareStatement("SELECT m.message, u.username, m.timestamp FROM Messages m JOIN Users u ON m.sender_id = u.id ORDER BY m.timestamp ASC");
                ResultSet rs = stmt.executeQuery();

                while (rs.next()) {
                    String message = rs.getString("message");
                    String username = rs.getString("username");
                    Timestamp timestamp = rs.getTimestamp("timestamp");
                    messages.add("[" + timestamp + "] " + username + ": " + message);
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
            return messages;
        }

        // Phương thức để broadcast tin nhắn đến tất cả các client khác
        private void broadcastMessage(String message) {
            for (ClientHandler clientHandler : clientHandlers) {
                clientHandler.out.println(message);
            }
        }
    }
}

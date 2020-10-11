import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;
import java.util.Vector;

public class CloudStorageServer {
    private List<ClientHandler> clients;
    final int PORT = 8189;

    public CloudStorageServer() {
        clients = new Vector<>();
        try (ServerSocket server = new ServerSocket(PORT)) {
            System.out.println("Сервер запущен.");
            while(true){
                Socket socket = server.accept();
                System.out.println("Клиент подключился.");
                new ClientHandler(this, socket);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

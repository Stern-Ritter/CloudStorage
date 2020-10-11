import java.io.IOException;
import java.net.Socket;
import java.net.SocketException;
import java.nio.file.Path;
import java.nio.file.Paths;


public class ClientHandler {
    private CloudStorageServer server;
    private Socket socket;
    Path clientPath;
    //Channel объявление

    public ClientHandler(CloudStorageServer server, Socket socket){
        this.server = server;
        this.socket = socket;
        try{
            //Открываем из сокета channel?
            new Thread(() -> {
                try {
                    socket.setSoTimeout(600);
                    //Цикл работы
                    server.subscribe(this);
                    clientPath = Paths.get("C:\\CloudStorageServer\\user1");
                    while (true){
                        //Работа
                    }
                } catch (SocketException ex) {
                    ex.printStackTrace();
                } catch (IOException ex) {
                    ex.printStackTrace();
                } finally {
                    server.unsubsribe(this);
                    System.out.println("Клиент отключился");
                    try{
                        socket.close();
                    } catch (IOException ex){
                        ex.printStackTrace();
                    }
                }
            }).start();
        }/* catch (IOException ex){
            ex.printStackTrace();
        }*/
    }
}

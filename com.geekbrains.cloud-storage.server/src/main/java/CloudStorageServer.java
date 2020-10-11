import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.util.List;
import java.util.Vector;

public class CloudStorageServer {
    private ServerSocketChannel serverSocketChannel;
    private Selector selector;
    private ByteBuffer buf = ByteBuffer.allocate(256);
    private int acceptedClientIndex = 1;
    final int PORT = 8189;

    private List<ClientHandler> clients;

    public CloudStorageServer(){
        clients = new Vector<ClientHandler>();

        serverSocketChannel = ServerSocketChannel.open();
        serverSocketChannel.socket().bind(new InetSocketAddress(8189));
        serverSocketChannel.configureBlocking(false);
        selector = Selector.open();
        serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);





        try{
            server = new ServerSocket(PORT);
            System.out.println("Сервер запущен!");
            while(true){
                socket = server.accept();
                System.out.println("Клиент подключился.");
            }

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try{
                server.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void subscribe(ClientHandler clientHandler){
        clients.add(clientHandler);
    }

    public void unsubsribe(ClientHandler clientHandler){
        clients.remove(clientHandler);
    }


}

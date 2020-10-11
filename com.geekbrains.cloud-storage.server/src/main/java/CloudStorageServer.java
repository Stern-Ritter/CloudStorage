import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;


public class CloudStorageServer implements Runnable {
    final int PORT = 8189;
    Path clientPath = Paths.get("C:","CloudStorageServer", "user1");

    public CloudStorageServer() {
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("Сервер запущен.");
            Socket socket = serverSocket.accept();
            System.out.println("Клиент подключился.");
            ObjectInputStream in = new ObjectInputStream(socket.getInputStream());
            FilePackage inputPackage = (FilePackage) in.readObject();
            Files.write(Paths.get(clientPath.toAbsolutePath().toString(),inputPackage.getFilename()),inputPackage.getData(), StandardOpenOption.CREATE_NEW);
            socket.close();
            in.close();
            System.out.println("Файл получен.");
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {

    }
}

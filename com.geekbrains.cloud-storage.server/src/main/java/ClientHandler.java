import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;

public class ClientHandler {
    private CloudStorageServer server;
    private Socket socket;
    private ObjectInputStream in;
    private ObjectOutputStream out;
    Path clientPath = Paths.get("C:","CloudStorageServer", "user1");

    public ClientHandler(CloudStorageServer server, Socket socket){
        this.server = server;
        this.socket =socket;
        try{
            in = new ObjectInputStream(socket.getInputStream());
            out = new ObjectOutputStream(socket.getOutputStream());
            new Thread(() -> {
                try {
                    while (true) {
                        byte num = in.readByte();
                        System.out.println(num);
                        if (num == 15) {
                            try {
                                FilePackage inputPackage = (FilePackage) in.readObject();
                                Files.write(Paths.get(clientPath.toAbsolutePath().toString(), inputPackage.getFilename()), inputPackage.getData(), StandardOpenOption.CREATE);
                            } catch (ClassNotFoundException ex) {
                                ex.printStackTrace();
                            }
                        }
                        if(num == 16){
                            out.writeByte(16);
                            FileListPackage outputPackage = getFileList();
                            out.writeObject(outputPackage);
                        }
                    }
                }  catch (IOException ex){
                    ex.printStackTrace();
                } finally {
                    System.out.println("Клиент отключился.");
                    try{
                        socket.close();
                    } catch (IOException ex){
                        ex.printStackTrace();
                    }
                }
            }).start();
        } catch (IOException ex){
            ex.printStackTrace();
        }
    }
    public FileListPackage getFileList() {
        ArrayList<String> list = new ArrayList<>();
         if (Files.exists(clientPath)){
            try {
                Files.walkFileTree(clientPath, new FileVisitor<Path>() {
                    @Override
                    public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                        return FileVisitResult.CONTINUE;
                    }

                    @Override
                    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                        list.add(file.getFileName().toString());
                        return FileVisitResult.CONTINUE;
                    }

                    @Override
                    public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
                        return FileVisitResult.CONTINUE;
                    }

                    @Override
                    public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                        return FileVisitResult.CONTINUE;
                    }
                });
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
         return new FileListPackage(list);
    }
}

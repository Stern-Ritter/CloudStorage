import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.ListView;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;

public class CloudStorageController {
    @FXML
    public ListView<String> fileList;
    @FXML
    public ListView<String> serverFileList;

    Socket socket;
    final String IP_ADDRESS = "localhost";
    final int PORT = 8189;
    ObjectInputStream in;
    ObjectOutputStream out;

    AuthController controller;
    Path clientPath = Paths.get("C:","CloudStorage");
    Path selectedFilePath;
    String selectedServerFilePath;

    public void connect() {
        try {
            socket = new Socket(IP_ADDRESS, PORT);
            out = new ObjectOutputStream(socket.getOutputStream());
            in = new ObjectInputStream(socket.getInputStream());
            new Thread(() -> {
                try {
                    while(true){
                        byte num = in.readByte();
                        if(num == 16){
                            try {
                                FileListPackage inputPackage = (FileListPackage) in.readObject();
                                Platform.runLater(() -> {
                                    serverFileList.getItems().clear();
                                    serverFileList.getItems().addAll(inputPackage.getFileList());
                                });
                            } catch (ClassNotFoundException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                } catch (RuntimeException ex) {
                    ex.printStackTrace();
                } catch (IOException ex) {
                    ex.printStackTrace();
                } finally {
                    System.out.println("Клиент отключен.");
                    try {
                        socket.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }).start();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public void sendFile(){
        try {
            FilePackage filePackage = new FilePackage(selectedFilePath.toAbsolutePath().toString(), selectedFilePath.getFileName().toString());
            out.writeByte(15);
            out.writeObject(filePackage);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void deleteServerFile(){
        try {
        FilePackage filePackage = new FilePackage(selectedServerFilePath);
        out.writeByte(17);
        out.writeObject(filePackage);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void getFileList(){
        try {
            out.writeByte(16);
            out.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void clickFileList(){
        selectedFilePath = Paths.get(clientPath.toAbsolutePath().toString(), fileList.getSelectionModel().getSelectedItem());
      }

    public void clickServerFileList(){
        selectedServerFilePath = serverFileList.getSelectionModel().getSelectedItem();
        System.out.println(selectedServerFilePath);
    }

    public void deleteFile(){
        try{
            Files.delete(selectedFilePath);
        } catch (IOException ex){
            ex.printStackTrace();
        }
        updateFileList();
    }

    public void updateFileList() {
        fileList.getItems().clear();
        if (Files.exists(clientPath)){
            try {
                Files.walkFileTree(clientPath, new FileVisitor<Path>() {
                    @Override
                    public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                        return FileVisitResult.CONTINUE;
                    }

                    @Override
                    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                        fileList.getItems().add(file.getFileName().toString());
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
    }

}

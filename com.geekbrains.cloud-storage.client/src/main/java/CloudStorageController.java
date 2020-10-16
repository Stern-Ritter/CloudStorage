import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.channel.*;
import javafx.fxml.FXML;
import javafx.scene.control.ListView;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;

public class CloudStorageController {
    @FXML
    public ListView<String> fileList;
    @FXML
    public ListView<String> serverFileList;

    AuthController controller;
    Path clientPath = Paths.get("C:","CloudStorage");
    Path selectedFilePath;
    String selectedServerFilePath;

    public void sendFile(){
        try {
            CommandSender.sendFile(selectedFilePath, Network.getInstance().getCurrentChannel());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void sendDeleteCommand(){
        CommandSender.sendDeleteCommand(selectedServerFilePath, Network.getInstance().getCurrentChannel());
    }

    public void sendFileListRequest(){
        CommandSender.sendFileListRequest(Network.getInstance().getCurrentChannel());
    }

    public void clickFileListItem(){
        selectedFilePath = Paths.get(clientPath.toAbsolutePath().toString(), fileList.getSelectionModel().getSelectedItem());
      }

    public void clickServerFileListItem(){
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

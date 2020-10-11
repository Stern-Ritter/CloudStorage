import javafx.fxml.FXML;
import javafx.scene.control.ListView;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;

public class CloudStorageController {
    @FXML
    public ListView<String> fileList;

    Controller controller;
    Path clientPath = Paths.get("C:\\CloudStorage");
    Path selectedFilePath;

    public void clickFileList(){
        selectedFilePath = Paths.get("C:\\CloudStorage\\" + fileList.getSelectionModel().getSelectedItem());
    }

    public void deleteFile(){
        try{
            System.out.println(selectedFilePath);
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

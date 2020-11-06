package ru.stern.client;

import ru.stern.common.FileService;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.ListView;

import java.io.IOException;
import java.nio.file.*;
import java.util.List;

public class CloudStorageController {
    @FXML
    public ListView<String> fileList;
    @FXML
    public ListView<String> serverFileList;

    AuthController controller;
    private Path clientPath = Paths.get("C:","CloudStorage");
    private Path selectedFilePath;
    private String selectedServerFilePath;

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

    public void sendFileRequest(){
        CommandSender.sendFileRequest(selectedServerFilePath, Network.getInstance().getCurrentChannel());
    }

    public void clickFileListItem(){
        selectedFilePath = Paths.get(clientPath.toAbsolutePath().toString(), fileList.getSelectionModel().getSelectedItem());
      }

    public void clickServerFileListItem(){
        selectedServerFilePath = serverFileList.getSelectionModel().getSelectedItem();
    }

    public void deleteFile(){
        try {
            FileService.deleteFile(selectedFilePath);
        } catch (IOException e) {
            e.printStackTrace();
        }
        updateFileList();
    }

    public void updateFileList() {
        Platform.runLater(() -> {
            fileList.getItems().clear();
            fileList.getItems().addAll(FileService.getFileList(clientPath));
        });
    }

    public void updateServerFileList(List<String> list) {
        Platform.runLater(() -> {
            serverFileList.getItems().clear();
            serverFileList.getItems().addAll(list);
        });
    }
}

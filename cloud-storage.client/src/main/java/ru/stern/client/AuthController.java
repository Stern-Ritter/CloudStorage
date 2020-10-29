package ru.stern.client;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class AuthController implements Initializable {
    @FXML
    public TextField loginField;
    @FXML
    public PasswordField passwordField;

    private Stage cloudStorageStage;
    private CloudStorageController cloudStorageController;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        cloudStorageStage = createCloudStorageWindow();
    }

    private Stage createCloudStorageWindow(){
        Stage stage = null;
        try{
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/cloud.fxml"));
            Parent root = fxmlLoader.load();
            stage = new Stage();
            stage.setTitle("Cloud storage");
            stage.setScene(new Scene(root, 800, 640));
            stage.initStyle(StageStyle.UTILITY);
            stage.initModality(Modality.APPLICATION_MODAL);
            cloudStorageController = fxmlLoader.getController();
            cloudStorageController.controller = this;
            InClientHandler.cloudStorageController = cloudStorageController;
            InClientHandler.authController = this;
        } catch (IOException e){
            e.printStackTrace();
        }
        return stage;
    }

    public void tryАuthentication(){
        String login = loginField.getText().trim();
        String password = passwordField.getText().trim();
        if (login.length() != 0 && password.length() != 0) {
            CommandSender.sendLoginPassword(login, hashCode(password), Network.getInstance().getCurrentChannel());
        } else {
            failedAuthentication("Authentication", "Fill in the username and password fields.");
        }
    }

    public void showCloudStorageWindow(){
        Platform.runLater(() -> {
            cloudStorageStage.show();
            cloudStorageController.updateFileList();
            cloudStorageController.sendFileListRequest();
        });
    }

    public void failedAuthentication(String title, String msg){
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle(title);
            alert.setHeaderText(null);
            alert.setContentText(msg);
            alert.showAndWait();
        });
    }

    private int hashCode (String str){
        return str.hashCode() + 32;
    }

}
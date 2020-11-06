package ru.stern.client;

import javafx.application.Platform;
import javafx.event.EventHandler;
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
import javafx.stage.WindowEvent;
import ru.stern.common.Commands;

import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ResourceBundle;
import java.util.concurrent.CountDownLatch;

public class AuthController implements Initializable {
    @FXML
    public TextField loginField;
    @FXML
    public PasswordField passwordField;

    private Stage cloudStorageStage;
    private CloudStorageController cloudStorageController;
    private CountDownLatch connectionStarted;

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
            stage.setOnCloseRequest(new EventHandler<WindowEvent>() {
                @Override
                public void handle(WindowEvent event) {
                    System.out.println("PROCESS: Start disconnect from server.");
                    if(Network.getInstance().getCurrentChannel() != null){
                        CommandSender.sendDisconnectRequest(Network.getInstance().getCurrentChannel());
                        Network.getInstance().stop();
                    }
                }
            });
            cloudStorageController = fxmlLoader.getController();
            cloudStorageController.controller = this;
            InClientHandler.cloudStorageController = cloudStorageController;
            InClientHandler.authController = this;
        } catch (IOException e){
            e.printStackTrace();
        }
        return stage;
    }

    public void showCloudStorageWindow(){
        Platform.runLater(() -> {
            cloudStorageStage.show();
            cloudStorageController.updateFileList();
            cloudStorageController.sendFileListRequest();
        });
    }

    public void tryAuthentication(){
        String login = loginField.getText().trim();
        String password = passwordField.getText().trim();
        if (login.length() != 0 && password.length() != 0) {
            if (Network.getInstance().getCurrentChannel() == null || !Network.getInstance().getCurrentChannel().isOpen()) {
                connectionStarted = new CountDownLatch(1);
                Network.getInstance().start(connectionStarted);
                try {
                    connectionStarted.await();
                } catch (InterruptedException e) {
                    System.out.println("ERROR: Failed connect to the server.");
                    failedAction("Connection", "Failed connect to the server.");
                }
            }
            if (Network.getInstance().getCurrentChannel() != null) {
                CommandSender.sendLoginPassword(Commands.AUTH_REQUEST, login, getPasswordHash(login, password,
                        "SHA-256"), Network.getInstance().getCurrentChannel());
            }
        } else {
            failedAction("Authentication", "Fill in the username and password fields.");
        }
    }

    public void tryRegistration(){
        String login = loginField.getText().trim();
        String password = passwordField.getText().trim();
        if (login.length() != 0 && password.length() != 0) {
            if (Network.getInstance().getCurrentChannel() == null || !Network.getInstance().getCurrentChannel().isOpen()) {
                connectionStarted = new CountDownLatch(1);
                Network.getInstance().start(connectionStarted);
                try {
                    connectionStarted.await();
                } catch (InterruptedException e) {
                    System.out.println("ERROR: Failed connect to the server.");
                    failedAction("Connection", "Failed connect to the server.");
                }
            }
            if (Network.getInstance().getCurrentChannel() != null) {
                CommandSender.sendLoginPassword(Commands.REG_REQUEST, login, getPasswordHash(login, password,
                        "SHA-256"), Network.getInstance().getCurrentChannel());
            }
        } else {
            failedAction("Authentication", "Fill in the username and password fields.");
        }
    }

    private String getPasswordHash(String login, String password, String algorithm){
        String hashCode = null;
        try{
            MessageDigest md = MessageDigest.getInstance(algorithm);
            byte[] salt = new StringBuilder(login).reverse().toString().getBytes(StandardCharsets.UTF_8);
            md.update(salt);
            byte[] hashedPassword = md.digest(password.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : hashedPassword){
                sb.append(String.format("%02X", b));
            }
            hashCode = sb.toString();
        } catch (NoSuchAlgorithmException ex){
            System.out.println("ERROR: The encryption algorithm not found.");
        }
        return hashCode;
    }

    public void failedAction(String title, String msg){
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle(title);
            alert.setHeaderText(null);
            alert.setContentText(msg);
            alert.showAndWait();
        });
    }

}

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.URL;
import java.util.ResourceBundle;

public class AuthController implements Initializable {
    @FXML
    public TextField loginField;
    @FXML
    public PasswordField passwordField;

    Stage cloudStorageStage;
    CloudStorageController cloudStorageController;

    Socket socket;
    final String IP_ADDRESS = "localhost";
    final int PORT = 8189;
    ObjectInputStream in;
    ObjectOutputStream out;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        cloudStorageStage = createCloudStorageWindow();
    }

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
                                cloudStorageController.fileListServer.getItems().clear();
                                cloudStorageController.fileListServer.getItems().addAll(inputPackage.getFileList());
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

    private Stage createCloudStorageWindow(){
        Stage stage = null;
        try{
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("cloud.fxml"));
            Parent root = fxmlLoader.load();
            stage = new Stage();
            stage.setTitle("Cloud storage");
            stage.setScene(new Scene(root, 800, 640));
            stage.initStyle(StageStyle.UTILITY);
            stage.initModality(Modality.APPLICATION_MODAL);
            cloudStorageController = fxmlLoader.getController();
            cloudStorageController.controller = this;
        } catch (IOException e){
            e.printStackTrace();
        }
        return stage;
    }

    public void showCloudStorageWindow(){
            connect();
            cloudStorageStage.show();
    }
}

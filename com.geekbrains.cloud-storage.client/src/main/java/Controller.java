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
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.URL;
import java.util.ResourceBundle;

public class Controller implements Initializable {
    @FXML
    public TextField loginField;
    @FXML
    public PasswordField passwordField;

    Stage cloudStorageStage;

    Socket socket;
    final String IP_ADDRESS = "localhost";
    final int PORT = 8189;
    ObjectOutputStream out;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        cloudStorageStage = createCloudStorageWindow();
    }

    public void connect() throws IOException {
        socket = new Socket(IP_ADDRESS, PORT);
        out = new ObjectOutputStream(socket.getOutputStream());
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
            CloudStorageController cloudStorageController = fxmlLoader.getController();
            cloudStorageController.controller = this;
        } catch (IOException e){
            e.printStackTrace();
        }
        return stage;
    }

    public void showCloudStorageWindow(){
        try {
            connect();
            cloudStorageStage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

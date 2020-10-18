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
import java.net.URL;
import java.util.ResourceBundle;

public class AuthController implements Initializable {
    @FXML
    public TextField loginField;
    @FXML
    public PasswordField passwordField;

    Stage cloudStorageStage;
    CloudStorageController cloudStorageController;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        cloudStorageStage = createCloudStorageWindow();
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
            InClientHandler.cloudStorageController = cloudStorageController;
        } catch (IOException e){
            e.printStackTrace();
        }
        return stage;
    }

    public void showCloudStorageWindow(){
            cloudStorageStage.show();
    }
}

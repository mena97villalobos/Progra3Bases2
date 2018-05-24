package Controller;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

import java.net.URL;
import java.util.ResourceBundle;

public class ControllerMain implements Initializable {
    @FXML
    public TextField filePath;
    @FXML
    public TextField collName;
    @FXML
    public Button load;
    @FXML
    public TextArea console;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        load.setOnAction(event -> {

        });
    }
}

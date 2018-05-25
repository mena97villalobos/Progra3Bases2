package Controller;

import Model.MongoConnection;
import Model.XmltoJson;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import org.json.*;
import java.io.File;
import java.net.URL;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
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
    @FXML
    public TextField query;
    @FXML
    public Button search;

    private MongoConnection mongo = new MongoConnection();
    private XmltoJson conversor = new XmltoJson();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        load.setOnAction(event -> {
            String pathText = filePath.getText();
            String collection = collName.getText();
            if(collection != "") {
                mongo.crearCollection(collection);
                if (pathText != "") {
                    File file = new File(filePath.getText());
                    ArrayList<File> files = new ArrayList<>();
                    if (file.isDirectory()) {
                        files = recorrerDir(file);
                    } else {
                        files.add(file);
                    }
                    for (File f : files) {
                        String nombreSalida = "salidasJSON/" + f.getName().replace(".xml", "");
                        conversor.XMLtoJSON(f.getPath(), nombreSalida);
                    }
                } else
                    console.setText("File path needed");
            }
            else
                console.setText("Collection name needed");
        });

        search.setOnAction(event -> {
            String q = query.getText();
            if(isJSONValid(q)){

            }
        });
    }

    public ArrayList<File> recorrerDir(File f){
        File[] files = f.listFiles();
        ArrayList<File> returnValue = new ArrayList<>();
        for (File file : files) {
            if(file.isDirectory()){
                returnValue.addAll(recorrerDir(file));
            }
            else{
                returnValue.add(file);
            }
        }
        return returnValue;
    }

    public boolean isJSONValid(String test) {
        try {
            new JSONObject(test);
        } catch (JSONException ex) {
            // edited, to include @Arthur's comment
            // e.g. in case JSONArray is valid as well...
            try {
                new JSONArray(test);
            } catch (JSONException ex1) {
                return false;
            }
        }
        return true;
    }
}

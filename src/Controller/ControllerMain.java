package Controller;

import Model.ArrayFields;
import Model.MongoConnection;
import Model.XmltoJson;
import javafx.collections.FXCollections;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import org.json.*;
import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.ResourceBundle;

public class ControllerMain implements Initializable {
    @FXML
    public TextField filePath;
    @FXML
    public TextField collName;
    @FXML
    public Button load;
    @FXML
    public Button save;
    @FXML
    public TextArea console;
    @FXML
    public TextField query;
    @FXML
    public Button search;
    @FXML
    public ComboBox preQuery;
    @FXML
    public Button mapReduce;
    @FXML
    public TextField project;

    private MongoConnection mongo = new MongoConnection();
    private XmltoJson conversor = new XmltoJson();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        ArrayList<String> preQuerys = new ArrayList<>();
        preQuerys.add("{\"TOPICS\":{$all:[\"sugar\"]}, \"PLACES\":{$all:[\"indonesia\"]}}");
        preQuerys.add("{$text : {$search : \"\\\"colombia\\\" \\\"coffee\\\"\"}}");
        preQuery.setItems(FXCollections.observableArrayList(preQuerys));
        load.setDisable(true);
        filePath.setDisable(true);
        search.setDisable(true);
        query.setDisable(true);
        mapReduce.setDisable(true);

        save.setOnAction(event -> {
            String collection = collName.getText();
            if(collection != "") {
                mongo.crearCollection(collection);
                load.setDisable(false);
                filePath.setDisable(false);
                search.setDisable(false);
                query.setDisable(false);
                mapReduce.setDisable(false);
            }
            else
                consoleLog("Es necesario el nombre de la colección");
        });

        load.setOnAction(event -> {
            String pathText = filePath.getText();
            if (pathText != "") {
                filePath.setDisable(true);
                collName.setDisable(true);
                load.setDisable(true);
                save.setDisable(true);
                mapReduce.setDisable(true);
                search.setDisable(true);
                Task task = new Task() {
                    @Override
                    protected Void call() throws Exception {
                        File file = new File(filePath.getText());
                        ArrayList<File> files = new ArrayList<>();
                        ArrayList<File> creados = new ArrayList<>();
                        if (file.isDirectory()) {
                            this.updateMessage("Recorriendo directorios");
                            files = recorrerDir(file);
                        } else {
                            files.add(file);
                        }
                        for (File f : files) {
                            String nombreSalida = "salidasJSON/" + f.getName().replace(".xml", "");
                            this.updateMessage(console.getText() + "\n" + "Conviertiendo: " + f.getName());
                            creados.addAll(conversor.XMLtoJSON(f.getPath(), nombreSalida));
                        }
                        String message = console.getText() + "\n" + mongo.cargarDatos(creados);
                        this.updateMessage(message);
                        for (ArrayFields field : ArrayFields.values()) {
                            mongo.crearIndex(field.toString());
                            consoleLog(console.getText() + "\n" + "Creando indice para: " + field.toString());
                        }
                        mongo.createTextIndex();
                        this.updateMessage(console.getText() + "\nCreando indices de texto");
                        filePath.setDisable(false);
                        collName.setDisable(false);
                        load.setDisable(false);
                        mapReduce.setDisable(false);
                        search.setDisable(false);
                        return null;
                    }
                };
                Thread t = new Thread(task);
                console.textProperty().bind(task.messageProperty());
                t.start();
            }
            else
                console.setText("File path needed");
        });

        search.setOnAction(event -> {
            String projection = parseProject();
            String selectedPreQuery = (String) preQuery.getSelectionModel().getSelectedItem();
            String q = query.getText();
            if(q.equals("") && selectedPreQuery == null){
                consoleLog("No es posible realizar una busqueda");
            }
            else if(q.equals("")){
                q = selectedPreQuery;
                String message = "Resultados de consulta\n" + mongo.buscar(q, projection);
                consoleLog(message);
            }
            else if(isJSONValid(q)){
                String message = "Resultados de consulta\n" + mongo.buscar(q, projection);
                consoleLog(message);
            }
            else{
                consoleLog("No es posible realizar una busqueda");
            }
            preQuery.getSelectionModel().clearSelection();
        });

        mapReduce.setOnAction(event -> {
            String message = mongo.mapReduce();
            consoleLog(message);
        });
    }

    private ArrayList<File> recorrerDir(File f){
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

    private boolean isJSONValid(String test) {
        try {
            new JSONObject(test);
        } catch (JSONException ex) {
            try {
                new JSONArray(test);
            } catch (JSONException ex1) {
                return false;
            }
        }
        return true;
    }

    private void consoleLog(String message){
        final String messageFinal = message;
        Task task = new Task() {
            @Override
            protected Void call() throws Exception {
                this.updateMessage(messageFinal);
                return null;
            }
        };
        Thread t = new Thread(task);
        console.textProperty().bind(task.messageProperty());
        t.start();
    }

    private String parseProject(){
        String s = project.getText();
        if(s.equals(""))
            return "{_id:1, TITLE:1}";
        else if(isJSONValid(s))
            return s;
        else
            return "{_id:1, TITLE:1}";
    }
}

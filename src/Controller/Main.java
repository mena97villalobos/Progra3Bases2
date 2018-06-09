package Controller;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception{
        Parent root = FXMLLoader.load(getClass().getResource("../View/Main.fxml"));
        primaryStage.setTitle("Tarea Programada 3, Bases de Datos 2");
        primaryStage.setScene(new Scene(root, 940, 593));
        primaryStage.show();
    }


    public static void main(String[] args) {
        launch(args);
    }
}

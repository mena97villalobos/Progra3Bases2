package Model;

import javafx.scene.control.Alert;

public class Alertas {
    public static void invocarAlerta(String mensaje, Alert.AlertType tipo) {
        Alert nuevaAlerta = new Alert(tipo);
        nuevaAlerta.setTitle("");
        nuevaAlerta.setContentText(mensaje);
        nuevaAlerta.showAndWait();
    }
}

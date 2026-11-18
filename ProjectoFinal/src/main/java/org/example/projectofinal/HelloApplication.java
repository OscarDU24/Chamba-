package org.example.projectofinal;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class HelloApplication extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        java.net.URL fxml = HelloApplication.class.getResource("/org/example/projectofinal/view/evaluaciones/GestionEvaluaciones.fxml");
        java.net.URL css = HelloApplication.class.getResource("/org/example/projectofinal/view/evaluaciones/evaluaciones.css");

        if (fxml == null) throw new IOException("No se encontró GestionEvaluaciones.fxml");
        if (css == null) throw new IOException("No se encontró evaluaciones.css");

        FXMLLoader fxmlLoader = new FXMLLoader(fxml);
        Scene scene = new Scene(fxmlLoader.load(), 1100, 720);
        scene.getStylesheets().add(css.toExternalForm());
        stage.setTitle("Gestión de evaluaciones");
        stage.setScene(scene);
        stage.show();
    }
}

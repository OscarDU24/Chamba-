package org.example.projectofinal;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class PruebaPrincipal extends Application {

    @Override
    public void start(Stage stage) throws Exception {

        FXMLLoader loader = new FXMLLoader(
                getClass().getResource(
                        "/org/example/projectofinal/Principal.fxml"
                )
        );

        Scene scene = new Scene(loader.load());

        stage.setTitle("Sistema de Gestión Institucional");
        stage.setScene(scene);

        stage.setMinWidth(1100);
        stage.setMinHeight(700);

        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}

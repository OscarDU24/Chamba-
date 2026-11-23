package org.example.projectofinal;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.example.projectofinal.util.JpaUtil;

import java.io.IOException;

public class HelloApplication extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(HelloApplication.class.getResource("loginUsuario.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 900, 600);
        stage.setTitle("Iniciar sesión");
        stage.setScene(scene);
        stage.show();
    }

    /** Al cerrar la ventana se cierra también la conexión a la base de datos. */
    @Override
    public void stop() {
        JpaUtil.cerrarConexion();
    }
}

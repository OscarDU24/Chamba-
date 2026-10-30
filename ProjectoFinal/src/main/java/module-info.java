module org.example.projectofinal {
    requires javafx.controls;
    requires javafx.fxml;


    opens org.example.projectofinal to javafx.fxml;
    exports org.example.projectofinal;
}
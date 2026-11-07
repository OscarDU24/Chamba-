module org.example.projectofinal {
    requires javafx.controls;
    requires javafx.fxml;
    requires jakarta.persistence;
    requires org.hibernate.orm.core;


    opens org.example.projectofinal to javafx.fxml;
    exports org.example.projectofinal;
}
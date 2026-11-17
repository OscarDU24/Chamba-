module org.example.projectofinal {
    requires javafx.controls;
    requires javafx.fxml;
    requires jakarta.persistence;
    requires org.hibernate.orm.core;
    requires java.sql;

    opens org.example.projectofinal to javafx.fxml;
    opens org.example.projectofinal.model to org.hibernate.orm.core;
    opens org.example.projectofinal.controller to javafx.fxml;

    exports org.example.projectofinal;
}
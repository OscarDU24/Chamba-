module org.example.projectofinal {
    requires javafx.controls;
    requires javafx.fxml;
    requires jakarta.persistence;
    requires org.hibernate.orm.core;
    requires java.sql;
    requires java.base;

    opens org.example.projectofinal to javafx.fxml;
    opens org.example.projectofinal.controller to javafx.fxml;
    opens org.example.projectofinal.model to org.hibernate.orm.core, jakarta.persistence;

    exports org.example.projectofinal;
}

module com.arturoar.bplustree {
    
    requires javafx.controls;
    requires javafx.fxml;
    requires transitive javafx.graphics;
    requires java.xml;
    requires javafx.base;
    
    requires transitive MaterialFX;
    requires VirtualizedFX;

    requires org.kordamp.ikonli.fontawesome5;
    requires org.kordamp.ikonli.coreui;
    requires org.kordamp.ikonli.javafx;
    requires org.kordamp.ikonli.core;
    requires mfx.resources;

    // Abre los paquetes para el uso reflexivo por parte de javafx.fxml
    opens com.arturoar.ui to javafx.fxml;
    //opens com.arturoar.exceptions to javafx.fxml;
    opens com.arturoar.model to javafx.fxml;
    opens com.arturoar.controller to javafx.fxml;
    opens com.arturoar.view to javafx.fxml;
    opens com.arturoar.util to javafx.fxml;

    // Exporta los paquetes para que otros módulos puedan usarlos
    exports com.arturoar.ui;
    exports com.arturoar.model;
    exports com.arturoar.controller;
    exports com.arturoar.view;
    exports com.arturoar.util;
    //exports com.arturoar.exceptions;
}
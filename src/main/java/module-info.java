module com.arturoar.bplustree {
    requires javafx.controls;
    requires javafx.fxml;

    // Abre los paquetes para el uso reflexivo por parte de javafx.fxml
    opens com.arturoar.ui to javafx.fxml;
    //opens com.arturoar.exceptions to javafx.fxml;
    opens com.arturoar.model to javafx.fxml;

    // Exporta los paquetes para que otros módulos puedan usarlos
    exports com.arturoar.ui;
    exports com.arturoar.model;
    //exports com.arturoar.exceptions;
}
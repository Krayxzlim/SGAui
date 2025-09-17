module com.sga {
    requires java.net.http;
    requires com.fasterxml.jackson.databind;

    // Exporta tus paquetes para que JavaFX pueda usarlos
    requires javafx.controls;
    requires javafx.fxml;

    opens com.sga to javafx.fxml;
    opens com.sga.service to com.fasterxml.jackson.databind;
    exports com.sga;
    exports com.sga.service;
}

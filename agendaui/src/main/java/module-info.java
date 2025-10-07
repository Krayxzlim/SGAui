module com.sga {
    requires java.net.http;
    requires com.fasterxml.jackson.databind;

    requires javafx.controls;
    requires javafx.fxml;

    // Esto es lo que falta
    requires com.calendarfx.view;

    opens com.sga.controllers to javafx.fxml;
    opens com.sga to javafx.fxml;
    opens com.sga.model to javafx.fxml;
    opens com.sga.service to com.fasterxml.jackson.databind;

    exports com.sga;
    exports com.sga.service;
    exports com.sga.controllers;
    exports com.sga.model;
}

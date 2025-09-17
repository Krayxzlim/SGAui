package com.sga.controllers;

import com.sga.service.RESTClient;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.util.Map;

public class ReportesController {

    @FXML private TableView<Map<String,Object>> tableReportes;
    @FXML private TableColumn<Map<String,Object>, String> colNombre;
    @FXML private TableColumn<Map<String,Object>, String> colValor;
    @FXML private Button btnRefrescar, btnExportar;

    private RESTClient client;
    private final ObservableList<Map<String,Object>> data = FXCollections.observableArrayList();

    public void setClient(RESTClient client) { this.client = client; }

    @FunctionalInterface
    interface ActionWithException {
        void run() throws Exception;
    }

    @FXML
    public void initialize() {
        colNombre.setCellValueFactory(d -> new ReadOnlyStringWrapper(String.valueOf(d.getValue().get("nombre"))));
        colValor.setCellValueFactory(d -> new ReadOnlyStringWrapper(String.valueOf(d.getValue().get("valor"))));
        tableReportes.setItems(data);

        btnRefrescar.setOnAction(e -> loadData());
        btnExportar.setOnAction(e -> handleAction(this::exportarExcel));
    }

    private void handleAction(ActionWithException action) {
        try {
            action.run();
            loadData();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public void loadData() {
        try {
            data.clear();
            data.addAll(client.listReportes());
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void exportarExcel() throws Exception {
        javafx.stage.FileChooser chooser = new javafx.stage.FileChooser();
        chooser.setTitle("Guardar Reporte Excel");
        chooser.getExtensionFilters().add(
            new javafx.stage.FileChooser.ExtensionFilter("Archivos Excel (*.xlsx)", "*.xlsx")
        );
        java.io.File file = chooser.showSaveDialog(btnExportar.getScene().getWindow());

        if (file != null) {
            byte[] excel = client.downloadReportesExcel(); // nuevo método que devuelva byte[]
            java.nio.file.Files.write(file.toPath(), excel);
            System.out.println("✅ Reporte exportado en: " + file.getAbsolutePath());
        }
    }
}

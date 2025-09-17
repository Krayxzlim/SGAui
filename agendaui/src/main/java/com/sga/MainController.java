package com.sga;

import java.util.Map;

import com.sga.service.RESTClient;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

public class MainController {
    @FXML private TableView<Map<String,Object>> table;
    @FXML private TableColumn<Map<String,Object>, String> colId;
    @FXML private TableColumn<Map<String,Object>, String> colNombre;
    @FXML private TableColumn<Map<String,Object>, String> colDireccion;

    private static RESTClient client;

    public static void setClient(RESTClient c) {
        client = c;
    }

    public void initialize() {
        // Bind columnas a keys del Map
        colId.setCellValueFactory(cell ->
            new SimpleStringProperty(String.valueOf(cell.getValue().get("id"))));
        colNombre.setCellValueFactory(cell ->
            new SimpleStringProperty(String.valueOf(cell.getValue().get("nombre"))));
        colDireccion.setCellValueFactory(cell ->
            new SimpleStringProperty(String.valueOf(cell.getValue().get("direccion"))));

        reload();
    }

    public void reload() {
        try {
            var list = client.listColegios();
            ObservableList<Map<String,Object>> items = FXCollections.observableArrayList(list);
            table.setItems(items);
        } catch (Exception e) {
            showError(e.getMessage());
        }
    }

    public void crearColegio() {
        try {
            var nuevo = Map.of("nombre", "Colegio X", "direccion", "Calle Falsa 123");
            client.crearColegio(nuevo);
            reload();
        } catch (Exception e) {
            showError(e.getMessage());
        }
    }

    private void showError(String msg) {
        Alert a = new Alert(Alert.AlertType.ERROR, msg, ButtonType.OK);
        a.showAndWait();
    }
}

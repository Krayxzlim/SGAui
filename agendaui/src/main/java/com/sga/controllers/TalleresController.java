package com.sga.controllers;

import java.util.Map;

import com.sga.model.Taller;
import com.sga.service.RESTClient;

import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;

public class TalleresController {

    @FXML private TableView<Taller> tableTalleres;
    @FXML private TableColumn<Taller,Long> colId;
    @FXML private TableColumn<Taller,String> colNombre;
    @FXML private TableColumn<Taller,String> colDescripcion;

    @FXML private TextField tfNombre, tfDescripcion;
    @FXML private Button btnCrear, btnActualizar, btnEliminar, btnRefrescar;

    private RESTClient client;
    private ObservableList<Taller> talleres; // compartida

    public void setData(RESTClient client, ObservableList<Taller> talleres) {
        this.client = client;
        this.talleres = talleres;
        tableTalleres.setItems(talleres);
        loadData();
    }

    @FXML
    public void initialize() {
        colId.setCellValueFactory(d -> d.getValue().idProperty().asObject());
        colNombre.setCellValueFactory(d -> d.getValue().nombreProperty());
        colDescripcion.setCellValueFactory(d -> d.getValue().descripcionProperty());

        tableTalleres.getSelectionModel().selectedItemProperty().addListener((obs, oldSel, newSel) -> {
            if (newSel != null) {
                tfNombre.setText(newSel.getNombre());
                tfDescripcion.setText(newSel.getDescripcion());
            } else {
                tfNombre.clear();
                tfDescripcion.clear();
            }
        });

        btnRefrescar.setOnAction(e -> loadData());
        btnCrear.setOnAction(e -> handleAction(this::createTaller));
        btnActualizar.setOnAction(e -> handleAction(this::updateTaller));
        btnEliminar.setOnAction(e -> handleAction(this::deleteTaller));
    }

    private void handleAction(Action action) {
        try {
            action.run();
            loadData();
            tableTalleres.getSelectionModel().clearSelection();
        } catch (Exception e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR, e.getMessage(), ButtonType.OK);
            alert.showAndWait();
        }
    }

    public void loadData() {
        if (client == null) return;
        try {
            talleres.clear();
            client.listTalleres().forEach(t -> talleres.add(t));
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void createTaller() throws Exception {
        if (tfNombre.getText().isBlank()) throw new RuntimeException("El nombre es obligatorio");
        Map<String,String> body = Map.of(
            "nombre", tfNombre.getText(),
            "descripcion", tfDescripcion.getText()
        );
        client.crearTaller(body);
    }

    private void updateTaller() throws Exception {
        Taller t = tableTalleres.getSelectionModel().getSelectedItem();
        if (t == null) throw new RuntimeException("Seleccione un taller para actualizar");
        Map<String,String> body = Map.of(
            "nombre", tfNombre.getText(),
            "descripcion", tfDescripcion.getText()
        );
        client.actualizarTaller(t.getId(), body);

    }

    private void deleteTaller() throws Exception {
        Taller t = tableTalleres.getSelectionModel().getSelectedItem();
        if (t == null) throw new RuntimeException("Seleccione un taller para eliminar");
        client.eliminarTaller((t.getId()));
    }

    @FunctionalInterface
    interface Action { void run() throws Exception; }
}

package com.sga.controllers;

import com.sga.model.Taller;
import com.sga.service.RESTClient;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.util.Map;

public class TalleresController {

    @FXML private TableView<Taller> tableTalleres;
    @FXML private TableColumn<Taller,String> colId;
    @FXML private TableColumn<Taller,String> colNombre;
    @FXML private TableColumn<Taller,String> colDescripcion;
    @FXML private TextField tfNombre, tfDescripcion;
    @FXML private Button btnCrear, btnActualizar, btnEliminar, btnRefrescar;

    private RESTClient client;
    private final ObservableList<Taller> data = FXCollections.observableArrayList();

    public void setClient(RESTClient client) { this.client = client; }

    @FunctionalInterface
    interface ActionWithException {
        void run() throws Exception;
    }

    @FXML
    public void initialize() {
        colId.setCellValueFactory(d -> new ReadOnlyStringWrapper(String.valueOf(d.getValue().getId())));
        colNombre.setCellValueFactory(d -> new ReadOnlyStringWrapper(d.getValue().getNombre()));
        colDescripcion.setCellValueFactory(d -> new ReadOnlyStringWrapper(d.getValue().getDescripcion()));
        tableTalleres.setItems(data);

        btnRefrescar.setOnAction(e -> loadData());
        btnCrear.setOnAction(e -> handleAction(this::createTaller));
        btnActualizar.setOnAction(e -> handleAction(this::updateTaller));
        btnEliminar.setOnAction(e -> handleAction(this::deleteTaller));
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
            client.listTalleres().forEach(map -> data.add(
                    new Taller(
                            Long.valueOf(String.valueOf(map.get("id"))),
                            (String) map.get("nombre"),
                            (String) map.get("descripcion")
                    )
            ));
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void createTaller() throws Exception {
        client.crearTaller(Map.of(
                "nombre", tfNombre.getText(),
                "descripcion", tfDescripcion.getText()
        ));
    }

    private void updateTaller() throws Exception {
        Taller t = tableTalleres.getSelectionModel().getSelectedItem();
        if (t == null) return;
        client.actualizarTaller(t.getId(), Map.of(
                "nombre", tfNombre.getText(),
                "descripcion", tfDescripcion.getText()
        ));
    }

    private void deleteTaller() throws Exception {
        Taller t = tableTalleres.getSelectionModel().getSelectedItem();
        if (t == null) return;
        client.eliminarTaller(t.getId());
    }
}

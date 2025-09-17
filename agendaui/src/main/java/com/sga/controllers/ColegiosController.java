package com.sga.controllers;

import java.util.Map;

import com.sga.model.Colegio;
import com.sga.service.RESTClient;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;

public class ColegiosController {

    @FXML private TableView<Colegio> tableColegios;
    @FXML private TableColumn<Colegio,String> colId;
    @FXML private TableColumn<Colegio,String> colNombre;
    @FXML private TableColumn<Colegio,String> colDireccion;
    @FXML private TableColumn<Colegio,String> colContacto;

    @FXML private TextField tfNombre, tfDireccion, tfContacto;
    @FXML private Button btnCrear, btnActualizar, btnEliminar, btnRefrescar;

    private RESTClient client;
    private final ObservableList<Colegio> data = FXCollections.observableArrayList();

    public void setClient(RESTClient client) {
        this.client = client;
        loadData(); // cargar datos apenas se asigna el cliente
    }

    @FunctionalInterface
    interface ActionWithException {
        void run() throws Exception;
    }

    @FXML
    public void initialize() {
        // Configurar columnas
        colId.setCellValueFactory(d -> d.getValue().idProperty());
        colNombre.setCellValueFactory(d -> d.getValue().nombreProperty());
        colDireccion.setCellValueFactory(d -> d.getValue().direccionProperty());
        colContacto.setCellValueFactory(d -> d.getValue().contactoProperty());

        tableColegios.setItems(data);

        // Listener para selección
        tableColegios.getSelectionModel().selectedItemProperty().addListener((obs, oldSel, newSel) -> {
            if (newSel != null) {
                tfNombre.setText(newSel.getNombre());
                tfDireccion.setText(newSel.getDireccion());
                tfContacto.setText(newSel.getContacto());
            } else {
                tfNombre.clear();
                tfDireccion.clear();
                tfContacto.clear();
            }
        });

        // Botones
        btnRefrescar.setOnAction(e -> loadData());
        btnCrear.setOnAction(e -> handleAction(this::createColegio));
        btnActualizar.setOnAction(e -> handleAction(this::updateColegio));
        btnEliminar.setOnAction(e -> handleAction(this::deleteColegio));
    }

    private void handleAction(ActionWithException action) {
        try {
            action.run();
            loadData();
            tableColegios.getSelectionModel().clearSelection();
        } catch (Exception e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR, e.getMessage(), ButtonType.OK);
            alert.showAndWait();
        }
    }

    public void loadData() {
        if (client == null) return;

        try {
            data.clear();
            client.listColegios().forEach(map -> data.add(
                new Colegio(
                    String.valueOf(map.get("id")),
                    (String) map.get("nombre"),
                    (String) map.get("direccion"),
                    map.get("contacto") != null ? (String) map.get("contacto") : ""
                )
            ));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void createColegio() throws Exception {
        if (tfNombre.getText().isBlank())
            throw new RuntimeException("El nombre es obligatorio");

        Map<String,String> body = Map.of(
            "nombre", tfNombre.getText(),
            "direccion", tfDireccion.getText(),
            "contacto", tfContacto.getText()
        );

        client.crearColegio(body);
    }

    private void updateColegio() throws Exception {
        Colegio c = tableColegios.getSelectionModel().getSelectedItem();
        if (c == null)
            throw new RuntimeException("Debe seleccionar un colegio para actualizar");

        Map<String,String> body = Map.of(
            "nombre", tfNombre.getText(),
            "direccion", tfDireccion.getText(),
            "contacto", tfContacto.getText()
        );

        client.actualizarColegio(Long.parseLong(c.getId()), body);
    }

    private void deleteColegio() throws Exception {
        Colegio c = tableColegios.getSelectionModel().getSelectedItem();
        if (c == null)
            throw new RuntimeException("Debe seleccionar un colegio para eliminar");

        client.eliminarColegio(Long.parseLong(c.getId()));
    }
}

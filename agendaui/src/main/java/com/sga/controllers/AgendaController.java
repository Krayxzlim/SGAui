package com.sga.controllers;

import java.util.HashMap;
import java.util.Map;
import com.sga.model.Agenda;
import com.sga.service.RESTClient;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.Alert.AlertType;

public class AgendaController {

    @FXML private TableView<Agenda> tableAgenda;
    @FXML private TableColumn<Agenda,String> colId;
    @FXML private TableColumn<Agenda,String> colFecha;
    @FXML private TableColumn<Agenda,String> colHora;
    @FXML private TableColumn<Agenda,String> colTaller;
    @FXML private TableColumn<Agenda,String> colResponsable;

    @FXML private TextField tfFecha, tfHora, tfTaller, tfResponsable;
    @FXML private Button btnCrear, btnActualizar, btnEliminar, btnRefrescar;

    private RESTClient client;
    private final ObservableList<Agenda> data = FXCollections.observableArrayList();

    public void setClient(RESTClient c) { this.client = c; }

    @FXML
    public void initialize() {
        colId.setCellValueFactory(d -> d.getValue().idProperty());
        colFecha.setCellValueFactory(d -> d.getValue().fechaProperty());
        colHora.setCellValueFactory(d -> d.getValue().horaProperty());
        colTaller.setCellValueFactory(d -> d.getValue().tallerProperty());
        colResponsable.setCellValueFactory(d -> d.getValue().responsableProperty());

        tableAgenda.setItems(data);

        btnRefrescar.setOnAction(e -> loadData());
        btnCrear.setOnAction(e -> crear());
        btnActualizar.setOnAction(e -> actualizar());
        btnEliminar.setOnAction(e -> eliminar());
    }

    public void loadData() {
        try {
            data.clear();
            var list = client.listAgenda();
            for (var map : list) {
                Map<String,Object> tallerMap = (Map<String,Object>) map.get("taller");
                Map<String,Object> respMap = (Map<String,Object>) map.get("responsable");
                data.add(new Agenda(
                        String.valueOf(map.get("id")),
                        (String) map.get("fecha"),
                        (String) map.get("hora"),
                        tallerMap != null ? (String) tallerMap.get("nombre") : "",
                        respMap != null ? (String) respMap.get("nombre") : ""
                ));
            }
        } catch (Exception e) {
            alertError("Error cargando agenda", e);
        }
    }

    private void crear() {
        if (!validarCampos()) return;
        try {
            Map<String,String> body = new HashMap<>();
            body.put("fecha", tfFecha.getText());
            body.put("hora", tfHora.getText());
            body.put("taller", tfTaller.getText());
            body.put("responsable", tfResponsable.getText());
            client.crearAgenda(body);
            loadData();
            limpiarCampos();
        } catch (Exception e) {
            alertError("Error creando agenda", e);
        }
    }

    private void actualizar() {
        Agenda a = tableAgenda.getSelectionModel().getSelectedItem();
        if (a == null) return;
        if (!validarCampos()) return;
        try {
            Map<String,String> body = new HashMap<>();
            body.put("fecha", tfFecha.getText());
            body.put("hora", tfHora.getText());
            body.put("taller", tfTaller.getText());
            body.put("responsable", tfResponsable.getText());
            client.actualizarAgenda(Long.parseLong(a.getId()), body);
            loadData();
            limpiarCampos();
        } catch (Exception e) {
            alertError("Error actualizando agenda", e);
        }
    }

    private void eliminar() {
        Agenda a = tableAgenda.getSelectionModel().getSelectedItem();
        if (a == null) return;
        try {
            client.eliminarAgenda(Long.parseLong(a.getId()));
            loadData();
            limpiarCampos();
        } catch (Exception e) {
            alertError("Error eliminando agenda", e);
        }
    }

    private boolean validarCampos() {
        if (tfFecha.getText().isBlank() || tfHora.getText().isBlank() || tfTaller.getText().isBlank() || tfResponsable.getText().isBlank()) {
            new Alert(AlertType.WARNING, "Todos los campos deben completarse").showAndWait();
            return false;
        }
        return true;
    }

    private void limpiarCampos() {
        tfFecha.clear();
        tfHora.clear();
        tfTaller.clear();
        tfResponsable.clear();
    }

    private void alertError(String msg, Exception e) {
        e.printStackTrace();
        new Alert(AlertType.ERROR, msg + ": " + e.getMessage()).showAndWait();
    }
}

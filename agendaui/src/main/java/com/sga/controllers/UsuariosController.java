package com.sga.controllers;

import com.sga.model.Usuario;
import com.sga.service.RESTClient;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.util.Map;

public class UsuariosController {

    @FXML private TableView<Usuario> tableUsuarios;
    @FXML private TableColumn<Usuario,String> colId;
    @FXML private TableColumn<Usuario,String> colNombre;
    @FXML private TableColumn<Usuario,String> colEmail;
    @FXML private TableColumn<Usuario,String> colRol;
    @FXML private TextField tfNombre, tfEmail, tfPassword;
    @FXML private Button btnCrear, btnActualizar, btnEliminar, btnRefrescar;

    private RESTClient client;
    private final ObservableList<Usuario> data = FXCollections.observableArrayList();

    public void setClient(RESTClient client) { this.client = client; }

    @FunctionalInterface
    interface ActionWithException {
        void run() throws Exception;
    }


    @FXML
    public void initialize() {
        colId.setCellValueFactory(d -> new ReadOnlyStringWrapper(String.valueOf(d.getValue().getId())));
        colNombre.setCellValueFactory(d -> new ReadOnlyStringWrapper(d.getValue().getNombre()));
        colEmail.setCellValueFactory(d -> new ReadOnlyStringWrapper(d.getValue().getEmail()));
        colRol.setCellValueFactory(d -> new ReadOnlyStringWrapper(d.getValue().getRol()));
        tableUsuarios.setItems(data);

        btnRefrescar.setOnAction(e -> loadData());
        btnCrear.setOnAction(e -> handleAction(this::createUsuario));
        btnActualizar.setOnAction(e -> handleAction(this::updateUsuario));
        btnEliminar.setOnAction(e -> handleAction(this::deleteUsuario));
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
            client.listUsuarios().forEach(map -> data.add(
                    new Usuario(
                            Long.valueOf(String.valueOf(map.get("id"))),
                            (String) map.get("nombre"),
                            (String) map.get("email"),
                            (String) map.get("rol")
                    )
            ));
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void createUsuario() throws Exception {
        client.crearUsuario(Map.of(
                "nombre", tfNombre.getText(),
                "email", tfEmail.getText(),
                "password", tfPassword.getText()
        ));
    }

    private void updateUsuario() throws Exception {
        Usuario u = tableUsuarios.getSelectionModel().getSelectedItem();
        if (u == null) return;
        client.actualizarUsuario(u.getId(), Map.of(
                "nombre", tfNombre.getText(),
                "email", tfEmail.getText()
        ));
    }

    private void deleteUsuario() throws Exception {
        Usuario u = tableUsuarios.getSelectionModel().getSelectedItem();
        if (u == null) return;
        client.eliminarUsuario(u.getId());
    }
}

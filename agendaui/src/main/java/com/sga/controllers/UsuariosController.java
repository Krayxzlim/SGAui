package com.sga.controllers;

import java.util.HashMap;
import java.util.Map;

import com.sga.model.Usuario;
import com.sga.service.RESTClient;

import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;

public class UsuariosController {

    @FXML private TableView<Usuario> tableUsuarios;
    @FXML private TableColumn<Usuario,String> colId;
    @FXML private TableColumn<Usuario,String> colNombre;
    @FXML private TableColumn<Usuario,String> colEmail;
    @FXML private TableColumn<Usuario,String> colRol;
    @FXML private TextField tfNombre, tfEmail, tfPassword;
    @FXML private ComboBox<String> cbRol;
    @FXML private Button btnCrear, btnActualizar, btnEliminar, btnRefrescar;

    private RESTClient client;
    private final ObservableList<Usuario> data = FXCollections.observableArrayList();

    public void setClient(RESTClient client) { 
        this.client = client; 
    }


    @FunctionalInterface
    interface ActionWithException {
        void run() throws Exception;
    }

    @FXML
    public void initialize() {
        // Inicializamos columnas
        colId.setCellValueFactory(d -> new ReadOnlyStringWrapper(String.valueOf(d.getValue().getId())));
        colNombre.setCellValueFactory(d -> new ReadOnlyStringWrapper(d.getValue().getNombre()));
        colEmail.setCellValueFactory(d -> new ReadOnlyStringWrapper(d.getValue().getEmail()));
        colRol.setCellValueFactory(d -> new ReadOnlyStringWrapper(d.getValue().getRol()));
        tableUsuarios.setItems(data);

        // Inicializamos ComboBox de rol
        cbRol.setItems(FXCollections.observableArrayList("TALLERISTA", "SUPERVISOR", "ADMINISTRADOR"));

        // Listener para seleccionar usuario de la tabla
        tableUsuarios.getSelectionModel().selectedItemProperty().addListener((obs, oldSel, newSel) -> {
            if (newSel != null) {
                tfNombre.setText(newSel.getNombre());
                tfEmail.setText(newSel.getEmail());
                tfPassword.setText(""); // dejamos vacía la contraseña
                cbRol.setValue(newSel.getRol());
            }
        });

        // Botones
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
            showAlert("Error", e.getMessage());
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
        } catch (Exception e) { 
            e.printStackTrace();
            showAlert("Error", e.getMessage());
        }
    }

    private void createUsuario() throws Exception {
        client.crearUsuario(Map.of(
                "nombre", tfNombre.getText(),
                "email", tfEmail.getText(),
                "password", tfPassword.getText(),
                "rol", cbRol.getValue()
        ));
    }

    private void updateUsuario() throws Exception {
        Usuario u = tableUsuarios.getSelectionModel().getSelectedItem();
        if (u == null) return;

        Map<String,String> body = new HashMap<>();
        body.put("nombre", tfNombre.getText());
        body.put("email", tfEmail.getText());
        body.put("rol", cbRol.getValue());
        if (!tfPassword.getText().isEmpty()) {
            body.put("password", tfPassword.getText());
        }

        client.actualizarUsuario(u.getId(), body);
    }

    private void deleteUsuario() throws Exception {
        Usuario u = tableUsuarios.getSelectionModel().getSelectedItem();
        if (u == null) return;
        client.eliminarUsuario(u.getId());
    }

    private void showAlert(String title, String msg) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }
}

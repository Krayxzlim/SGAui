package com.sga.controllers;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.sga.model.Agenda;
import com.sga.model.Taller;
import com.sga.model.Usuario;
import com.sga.service.RESTClient;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.util.StringConverter;

public class AgendaController {

    @FXML private TableView<Agenda> tableAgenda;
    @FXML private TableColumn<Agenda, Long> colId;
    @FXML private TableColumn<Agenda, String> colFecha;
    @FXML private TableColumn<Agenda, String> colHora;
    @FXML private TableColumn<Agenda, String> colTaller;
    @FXML private TableColumn<Agenda, String> colTallerista;

    @FXML private DatePicker dpFecha;
    @FXML private TextField tfHora;
    @FXML private ComboBox<Taller> cbTaller;
    @FXML private ComboBox<Usuario> cbTallerista;

    @FXML private Button btnCrear;
    @FXML private Button btnActualizar;
    @FXML private Button btnEliminar;
    @FXML private Button btnRefrescar;

    private RESTClient client;
    private final ObservableList<Agenda> agendaList = FXCollections.observableArrayList();
    private final ObservableList<Taller> talleres = FXCollections.observableArrayList();
    private final ObservableList<Usuario> usuarios = FXCollections.observableArrayList();

    public void setClient(RESTClient client) {
        this.client = client;
        loadTalleres();
        loadUsuarios();
        loadData();
    }

    @FXML
    public void initialize() {
        // Configurar columnas
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colFecha.setCellValueFactory(new PropertyValueFactory<>("fecha"));
        colHora.setCellValueFactory(new PropertyValueFactory<>("hora"));
        colTaller.setCellValueFactory(cell -> 
            new SimpleStringProperty(cell.getValue().getTaller() != null ?
                cell.getValue().getTaller().getNombre() : "")
        );
        colTallerista.setCellValueFactory(cell ->
            new SimpleStringProperty(cell.getValue().getTallerista() != null ?
                cell.getValue().getTallerista().getNombre() : "")
        );

        // Botones
        btnCrear.setOnAction(e -> crear());
        btnActualizar.setOnAction(e -> actualizar());
        btnEliminar.setOnAction(e -> eliminar());
        btnRefrescar.setOnAction(e -> loadData());

        // Selección de tabla
        tableAgenda.getSelectionModel().selectedItemProperty().addListener(
            (obs, oldSel, newSel) -> fillForm(newSel)
        );
    }

    private void loadTalleres() {
        try {
            List<Taller> lista = client.listTalleres();
            talleres.setAll(lista);
            cbTaller.setItems(talleres);
            cbTaller.setConverter(new StringConverter<>() {
                @Override public String toString(Taller t) { return t != null ? t.getNombre() : ""; }
                @Override public Taller fromString(String s) { return null; }
            });
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void loadUsuarios() {
        try {
            List<Usuario> lista = client.listUsuarios();
            usuarios.setAll(lista);
            cbTallerista.setItems(usuarios);
            cbTallerista.setConverter(new StringConverter<>() {
                @Override public String toString(Usuario u) { return u != null ? u.getNombre() : ""; }
                @Override public Usuario fromString(String s) { return null; }
            });
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void loadData() {
        try {
            List<Agenda> lista = client.listAgendas();
            agendaList.setAll(lista);
            tableAgenda.setItems(agendaList);
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void fillForm(Agenda agenda) {
        if (agenda == null) return;
        dpFecha.setValue(LocalDate.parse(agenda.getFecha()));
        tfHora.setText(agenda.getHora());
        cbTaller.getSelectionModel().select(agenda.getTaller());
        cbTallerista.getSelectionModel().select(agenda.getTallerista());
    }

    private boolean validarCampos() {
        if (dpFecha.getValue() == null || tfHora.getText().isEmpty() ||
            cbTaller.getValue() == null || cbTallerista.getValue() == null) {
            alertError("Error", "Todos los campos son obligatorios");
            return false;
        }
        return true;
    }

    private void crear() {
        if (!validarCampos()) return;
        try {
            Map<String,Object> body = new HashMap<>();
            body.put("fecha", dpFecha.getValue().toString()); 
            body.put("hora", tfHora.getText());
            body.put("taller", Map.of("id", cbTaller.getValue().getId()));
            body.put("tallerista", Map.of("id", cbTallerista.getValue().getId()));

            client.crearAgenda(body);
            loadData();
            limpiarCampos();
        } catch (Exception e) { alertError("Error creando agenda", e); }
    }

    private void actualizar() {
        Agenda selected = tableAgenda.getSelectionModel().getSelectedItem();
        if (selected == null) { alertError("Error", "Seleccione una agenda"); return; }
        if (!validarCampos()) return;

        try {
            Map<String,Object> body = new HashMap<>();
            body.put("fecha", dpFecha.getValue().toString());
            body.put("hora", tfHora.getText());
            body.put("taller", Map.of("id", cbTaller.getValue().getId()));
            body.put("Tallerista", Map.of("id", cbTallerista.getValue().getId()));

            client.actualizarAgenda(selected.getId(), body);
            loadData();
            limpiarCampos();
        } catch (Exception e) { alertError("Error actualizando agenda", e); }
    }

    private void eliminar() {
        Agenda selected = tableAgenda.getSelectionModel().getSelectedItem();
        if (selected == null) { alertError("Error", "Seleccione una agenda"); return; }
        try {
            client.eliminarAgenda(selected.getId());
            loadData();
            limpiarCampos();
        } catch (Exception e) { alertError("Error eliminando agenda", e); }
    }

    private void limpiarCampos() {
        dpFecha.setValue(null);
        tfHora.clear();
        cbTaller.getSelectionModel().clearSelection();
        cbTallerista.getSelectionModel().clearSelection();
    }

    private void alertError(String title, Object msg) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(msg.toString());
        alert.showAndWait();
    }
}

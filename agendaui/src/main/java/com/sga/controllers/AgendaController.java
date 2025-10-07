package com.sga.controllers;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

import com.calendarfx.model.Calendar;
import com.calendarfx.model.CalendarSource;
import com.calendarfx.model.Entry;
import com.calendarfx.view.CalendarView;
import com.sga.model.Agenda;
import com.sga.model.Taller;
import com.sga.model.Usuario;
import com.sga.service.RESTClient;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextField;
import javafx.util.StringConverter;

public class AgendaController {

    @FXML private DatePicker dpFecha;
    @FXML private TextField tfHora;
    @FXML private ComboBox<Taller> cbTaller;
    @FXML private ComboBox<Usuario> cbTallerista;

    @FXML private Button btnCrear;
    @FXML private Button btnActualizar;
    @FXML private Button btnEliminar;
    @FXML private Button btnRefrescar;

    @FXML private CalendarView calendarView;

    private RESTClient client;
    private final ObservableList<Taller> talleres = FXCollections.observableArrayList();
    private final ObservableList<Usuario> usuarios = FXCollections.observableArrayList();

    private Calendar<Agenda> agendaCalendar = new Calendar<>("Agendas");


    private Agenda selectedAgenda;

    public void setClient(RESTClient client) {
        this.client = client;
        loadTalleres();
        loadUsuarios();
        loadCalendar();
    }

    @FXML
    public void initialize() {
        // Configura ComboBoxes
        cbTaller.setConverter(new StringConverter<>() {
            @Override public String toString(Taller t) { return t != null ? t.getNombre() : ""; }
            @Override public Taller fromString(String s) { return null; }
        });

        cbTallerista.setConverter(new StringConverter<>() {
            @Override public String toString(Usuario u) { return u != null ? u.getNombre() : ""; }
            @Override public Usuario fromString(String s) { return null; }
        });

        // Botones
        btnCrear.setOnAction(e -> crear());
        btnActualizar.setOnAction(e -> actualizar());
        btnEliminar.setOnAction(e -> eliminar());
        btnRefrescar.setOnAction(e -> loadCalendar());

        // Calendario
        calendarView.getCalendarSources().clear();
        CalendarSource source = new CalendarSource("Mis Calendarios");
        source.getCalendars().add(agendaCalendar);
        calendarView.getCalendarSources().add(source);
        calendarView.setRequestedTime(LocalTime.now());

        // Selección de Entry
        agendaCalendar.addEventHandler(evt -> {
            @SuppressWarnings("unchecked")
            Entry<Agenda> e = (Entry<Agenda>) evt.getEntry();
            if (e != null) {
                selectedAgenda = e.getUserObject();
                fillForm(selectedAgenda);
            }
        });
    }


    private void loadTalleres() {
        try {
            List<Taller> lista = client.listTalleres();
            talleres.setAll(lista);
            cbTaller.setItems(talleres);
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void loadUsuarios() {
        try {
            List<Usuario> lista = client.listUsuarios();
            usuarios.setAll(lista);
            cbTallerista.setItems(usuarios);
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void loadCalendar() {
        try {
            agendaCalendar.clear();
            List<Agenda> lista = client.listAgendas();
            DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss");

            for (Agenda a : lista) {
                LocalDate date = LocalDate.parse(a.getFecha());
                LocalTime time = LocalTime.parse(a.getHora(), timeFormatter);

                Entry<Agenda> entry = new Entry<>(a.getTaller().getNombre() + " - " + a.getTallerista().getNombre());
                entry.setInterval(date.atTime(time), date.atTime(time.plusHours(1)));
                entry.setUserObject(a);
                agendaCalendar.addEntry(entry);
            }
        } catch (Exception e) {
            alertError("Error cargando calendario", e);
        }
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
            Map<String,Object> body = Map.of(
                "fecha", dpFecha.getValue().toString(),
                "hora", tfHora.getText(),
                "taller", Map.of("id", cbTaller.getValue().getId()),
                "tallerista", Map.of("id", cbTallerista.getValue().getId())
            );

            client.crearAgenda(body);
            loadCalendar();
            limpiarCampos();
        } catch (Exception e) { alertError("Error creando agenda", e); }
    }

    private void actualizar() {
        if (selectedAgenda == null) { alertError("Error", "Seleccione una agenda"); return; }
        if (!validarCampos()) return;

        try {
            Map<String,Object> body = Map.of(
                "fecha", dpFecha.getValue().toString(),
                "hora", tfHora.getText(),
                "taller", Map.of("id", cbTaller.getValue().getId()),
                "tallerista", Map.of("id", cbTallerista.getValue().getId())
            );

            client.actualizarAgenda(selectedAgenda.getId(), body);
            loadCalendar();
            limpiarCampos();
        } catch (Exception e) { alertError("Error actualizando agenda", e); }
    }

    private void eliminar() {
        if (selectedAgenda == null) { alertError("Error", "Seleccione una agenda"); return; }
        try {
            client.eliminarAgenda(selectedAgenda.getId());
            loadCalendar();
            limpiarCampos();
        } catch (Exception e) { alertError("Error eliminando agenda", e); }
    }

    private void limpiarCampos() {
        dpFecha.setValue(null);
        tfHora.clear();
        cbTaller.getSelectionModel().clearSelection();
        cbTallerista.getSelectionModel().clearSelection();
        selectedAgenda = null;
    }

    private void alertError(String title, Object msg) {
        Alert alert = new Alert(AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(msg.toString());
        alert.showAndWait();
    }
}

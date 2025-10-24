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
import com.sga.model.Colegio;
import com.sga.model.Taller;
import com.sga.service.RESTClient;

import javafx.collections.ObservableList;
import javafx.collections.ObservableSet;
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
    @FXML private ComboBox<Colegio> cbColegio;
    @FXML private Button btnCrear;
    @FXML private Button btnActualizar;
    @FXML private Button btnEliminar;
    @FXML private Button btnRefrescar;
    @FXML private CalendarView calendarView;

    private RESTClient client;
    private ObservableList<Taller> talleres;
    private ObservableList<Colegio> colegios;
    private final Calendar<Agenda> agendaCalendar = new Calendar<>("Agendas");

    private Agenda selectedAgenda;

    // Inyección de listas compartidas
    public void setData(RESTClient client, ObservableList<Taller> talleres, ObservableList<Colegio> colegios) {
        this.client = client;
        this.talleres = talleres;
        this.colegios = colegios;

        cbTaller.setItems(talleres);
        cbColegio.setItems(colegios);

        loadCalendar();
    }

    @FXML
    public void initialize() {
        cbTaller.setConverter(new StringConverter<>() {
            @Override public String toString(Taller t) { return t != null ? t.getNombre() : ""; }
            @Override public Taller fromString(String s) { return null; }
        });

        cbColegio.setConverter(new StringConverter<>() {
            @Override public String toString(Colegio c) { return c != null ? c.getNombre() : ""; }
            @Override public Colegio fromString(String s) { return null; }
        });

        btnCrear.setOnAction(e -> crear());
        btnActualizar.setOnAction(e -> actualizar());
        btnEliminar.setOnAction(e -> eliminar());
        btnRefrescar.setOnAction(e -> loadCalendar());

        calendarView.getCalendarSources().clear();
        CalendarSource source = new CalendarSource("Mis Calendarios");
        source.getCalendars().add(agendaCalendar);
        calendarView.getCalendarSources().add(source);
        calendarView.setRequestedTime(LocalTime.now());

        agendaCalendar.addEventHandler(evt -> {
            @SuppressWarnings("unchecked")
            Entry<Agenda> entry = (Entry<Agenda>) evt.getEntry();
            if (entry != null && entry.getUserObject() != null) {
                selectedAgenda = entry.getUserObject();
                fillForm(selectedAgenda);
            }
        });

        agendaCalendar.addEventHandler(evt -> {
            Entry<?> entry = evt.getEntry();
            if (entry == null) return;

            var type = evt.getEventType();
            switch (type.getName()) {
                case "ENTRY_INTERVAL_CHANGED":
                case "ENTRY_TITLE_CHANGED":
                case "ENTRY_START_TIME_CHANGED":
                case "ENTRY_END_TIME_CHANGED":
                    handleCalendarEntrySave(entry);
                    break;
                case "ENTRY_REMOVED":
                    handleCalendarEntryDelete(entry);
                    break;
            }
        });

        calendarView.addEventFilter(javafx.scene.input.KeyEvent.KEY_PRESSED, event -> {
            if (event.getCode() == javafx.scene.input.KeyCode.DELETE) {
                ObservableSet<Entry<?>> selectedEntries = calendarView.getSelections();
                if (!selectedEntries.isEmpty()) {
                    Entry<?> selectedEntry = selectedEntries.iterator().next();
                    handleCalendarEntryDelete(selectedEntry);
                    loadCalendar();
                    limpiarCampos();
                    event.consume();
                }
            }
        });
    }

    private void loadCalendar() {
        if (client == null) return;
        try {
            agendaCalendar.clear();
            List<Agenda> lista = client.listAgendas();
            DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss");

            for (Agenda a : lista) {
                LocalDate date = LocalDate.parse(a.getFecha());
                LocalTime time = LocalTime.parse(a.getHora(), timeFormatter);

                Entry<Agenda> entry = new Entry<>(a.getTaller().getNombre() + " - " + a.getColegio().getNombre());
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
        cbColegio.getSelectionModel().select(agenda.getColegio());
    }

    private boolean validarCampos() {
        if (dpFecha.getValue() == null || tfHora.getText().isEmpty()
                || cbTaller.getValue() == null || cbColegio.getValue() == null) {
            alertError("Error", "Todos los campos son obligatorios");
            return false;
        }
        return true;
    }

    private void crear() {
        if (!validarCampos()) return;
        try {
            Map<String, Object> body = Map.of(
                "fecha", dpFecha.getValue().toString(),
                "hora", tfHora.getText(),
                "taller", Map.of("id", cbTaller.getValue().getId()),
                "colegio", Map.of("id", cbColegio.getValue().getId())
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
            Map<String, Object> body = Map.of(
                "fecha", dpFecha.getValue().toString(),
                "hora", tfHora.getText(),
                "taller", Map.of("id", cbTaller.getValue().getId()),
                "colegio", Map.of("id", cbColegio.getValue().getId())
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
        cbColegio.getSelectionModel().clearSelection();
        selectedAgenda = null;
    }

    private void handleCalendarEntrySave(Entry<?> entry) {
        try {
            LocalDate date = entry.getStartDate();
            LocalTime time = entry.getStartTime();
            String title = entry.getTitle();
            if (title == null || !title.contains("-")) return;

            String[] partes = title.split("-", 2);
            Taller taller = talleres.stream().filter(t -> t.getNombre().equalsIgnoreCase(partes[0].trim())).findFirst().orElse(null);
            Colegio colegio = colegios.stream().filter(c -> c.getNombre().equalsIgnoreCase(partes[1].trim())).findFirst().orElse(null);
            if (taller == null || colegio == null) return;

            Map<String, Object> body = Map.of(
                "fecha", date.toString(),
                "hora", time.toString(),
                "taller", Map.of("id", taller.getId()),
                "colegio", Map.of("id", colegio.getId())
            );

            if (entry.getUserObject() != null) {
                client.actualizarAgenda(((Agenda) entry.getUserObject()).getId(), body);
            } else {
                client.crearAgenda(body);
            }

            loadCalendar();
        } catch (Exception e) { alertError("Error guardando evento", e); }
    }

    private void handleCalendarEntryDelete(Entry<?> entry) {
        try {
            if (entry.getUserObject() != null)
                client.eliminarAgenda(((Agenda) entry.getUserObject()).getId());
        } catch (Exception e) { alertError("Error eliminando evento", e); }
    }

    private void alertError(String title, Object msg) {
        Alert alert = new Alert(AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(msg.toString());
        alert.showAndWait();
    }
}

package com.sga.controllers;

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
import javafx.fxml.FXML;
import javafx.scene.control.Alert;


public class AgendaController {

    @FXML private CalendarView calendarView;

    private RESTClient client;
    private final Calendar<Agenda> agendaCalendar = new Calendar<>("Agendas");
    private ObservableList<Taller> talleres;
    private ObservableList<Colegio> colegios;


    // Inyección del cliente
    public void setClient(RESTClient client) {
        this.client = client;
        loadCalendar();
    }

    @FXML
    public void initialize() {
        calendarView.getCalendarSources().clear();
        CalendarSource source = new CalendarSource("Mis Calendarios");
        source.getCalendars().add(agendaCalendar);
        calendarView.getCalendarSources().add(source);
        calendarView.setRequestedTime(LocalTime.now());

        // Manejo de eventos del calendario
        agendaCalendar.addEventHandler(evt -> {
            Entry<?> entry = evt.getEntry();
            if (entry == null) return;

            switch (evt.getEventType().getName()) {
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
                if (!calendarView.getSelections().isEmpty()) {
                    Entry<?> selectedEntry = calendarView.getSelections().iterator().next();
                    handleCalendarEntryDelete(selectedEntry);
                    loadCalendar();
                    event.consume();
                }
            }
        });
    }

    public void setData(RESTClient client, ObservableList<Taller> talleres, ObservableList<Colegio> colegios) {
        this.client = client;
        this.talleres = talleres;
        this.colegios = colegios;
        loadCalendar();
    }


    private void loadCalendar() {
        if (client == null) return;
        try {
            agendaCalendar.clear();
            List<Agenda> lista = client.listAgendas();
            DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss");

            for (Agenda a : lista) {
                var date = java.time.LocalDate.parse(a.getFecha());
                var time = java.time.LocalTime.parse(a.getHora(), timeFormatter);

                Entry<Agenda> entry = new Entry<>(a.getTaller().getNombre() + " - " + a.getColegio().getNombre());
                entry.setInterval(date.atTime(time), date.atTime(time.plusHours(1)));
                entry.setUserObject(a);
                agendaCalendar.addEntry(entry);
            }
        } catch (Exception e) {
            alertError("Error cargando calendario", e);
        }
    }

    private void handleCalendarEntrySave(Entry<?> entry) {
        try {
            //el título tiene que seguir el orden "Taller - Colegio"
            String[] partes = entry.getTitle().split(" - ", 2);
            if (partes.length < 2) return;

            Taller taller = talleres.stream()
                    .filter(t -> t.getNombre().equalsIgnoreCase(partes[0].trim()))
                    .findFirst().orElse(null);

            Colegio colegio = colegios.stream()
                    .filter(c -> c.getNombre().equalsIgnoreCase(partes[1].trim()))
                    .findFirst().orElse(null);

            if (taller == null || colegio == null) return;

            if (entry.getUserObject() != null) {
                // UPDATE
                Agenda agenda = (Agenda) entry.getUserObject();
                agenda.setFecha(entry.getStartDate().toString());
                agenda.setHora(entry.getStartTime().toString());
                agenda.setTaller(taller);
                agenda.setColegio(colegio);

                client.actualizarAgenda(agenda.getId(), Map.of(
                        "fecha", agenda.getFecha(),
                        "hora", agenda.getHora(),
                        "taller", Map.of("id", taller.getId()),
                        "colegio", Map.of("id", colegio.getId())
                ));
            } else {
                // CREATE
                client.crearAgenda(Map.of(
                        "fecha", entry.getStartDate().toString(),
                        "hora", entry.getStartTime().toString(),
                        "taller", Map.of("id", taller.getId()),
                        "colegio", Map.of("id", colegio.getId())
                ));
            }

            // Siempre recargamos el calendario
            loadCalendar();

        } catch (Exception e) {
            alertError("Error guardando evento", e);
        }
    }

    private void handleCalendarEntryDelete(Entry<?> entry) {
        try {
            if (entry.getUserObject() != null) {
                client.eliminarAgenda(((Agenda) entry.getUserObject()).getId());
            }
        } catch (Exception e) {
            alertError("Error eliminando evento", e);
        }
    }

    private void alertError(String title, Object msg) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(msg.toString());
        alert.showAndWait();
    }
}

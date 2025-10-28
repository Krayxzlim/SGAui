package com.sga.controllers;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import com.calendarfx.model.Calendar;
import com.calendarfx.model.CalendarSource;
import com.calendarfx.model.Entry;
import com.calendarfx.view.CalendarView;
import com.sga.model.Agenda;
import com.sga.model.Colegio;
import com.sga.model.Taller;
import com.sga.model.Usuario;
import com.sga.service.RESTClient;

import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.layout.GridPane;

public class AgendaController {

@FXML private CalendarView calendarView;

private RESTClient client;
private final Calendar<Agenda> agendaCalendar = new Calendar<>("Agendas");
private ObservableList<Taller> talleres;
private ObservableList<Colegio> colegios;

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

    // Eventos del calendario
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

    // Eliminar con tecla Supr
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

    calendarView.setEntryFactory(param -> {
        LocalDate date = param.getZonedDateTime().toLocalDate();
        Entry<Agenda> newEntry = showNewAgendaDialog(date, null);
        if (newEntry == null) {
            Entry<Agenda> dummy = new Entry<>("Cancelado");
            dummy.setInterval(date.atStartOfDay(), date.atStartOfDay().plusMinutes(1));
            return dummy;
        }
        return newEntry;
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
            LocalDate date = LocalDate.parse(a.getFecha());
            LocalTime time = LocalTime.parse(a.getHora(), timeFormatter);

            // Obtener talleristas asignados
            List<Map<String, Object>> asignados = client.obtenerTalleristasPorAgenda(a.getId());
            List<String> nombresTalleristas = Optional.ofNullable(asignados)
                .orElse(Collections.emptyList())
                .stream()
                .map(t -> {
                    Object talleristaObj = t.get("tallerista");
                    if (talleristaObj instanceof Map<?, ?> talleristaMap) {
                        Object nombreObj = talleristaMap.get("nombre");
                        return nombreObj != null ? nombreObj.toString() : "";
                    }
                    return "";
                })
                .filter(nombre -> !nombre.isBlank())
                .limit(2)
                .collect(Collectors.toList());

            System.out.println("Nombres detectados: " + nombresTalleristas);

            String titulo = a.getColegio().getNombre() + " - " + a.getTaller().getNombre();
            if (!nombresTalleristas.isEmpty()) {
                titulo += " - " + String.join(" - ", nombresTalleristas);
            }

            Entry<Agenda> entry = new Entry<>(titulo);
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
        String[] partes = entry.getTitle().split(" - ");
        if (partes.length < 2) return;

        Colegio colegio = colegios.stream()
                .filter(c -> c.getNombre().equalsIgnoreCase(partes[0].trim()))
                .findFirst().orElse(null);

        Taller taller = talleres.stream()
                .filter(t -> t.getNombre().equalsIgnoreCase(partes[1].trim()))
                .findFirst().orElse(null);

        if (colegio == null || taller == null) return;

        Agenda agenda;
        if (entry.getUserObject() != null) {
            agenda = (Agenda) entry.getUserObject();
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
            client.crearAgenda(Map.of(
                    "fecha", entry.getStartDate().toString(),
                    "hora", entry.getStartTime().toString(),
                    "taller", Map.of("id", taller.getId()),
                    "colegio", Map.of("id", colegio.getId())
            ));
            agenda = new Agenda();
        }

        List<Map<String, Object>> asignados = client.obtenerTalleristasPorAgenda(agenda.getId());
        List<String> nombresTalleristas = Optional.ofNullable(asignados)
            .orElse(Collections.emptyList())
            .stream()
            .map(t -> {
                Object talleristaObj = t.get("tallerista");
                if (talleristaObj instanceof Map<?, ?> talleristaMap) {
                    Object nombreObj = talleristaMap.get("nombre");
                    return nombreObj != null ? nombreObj.toString() : "";
                }
                return "";
            })
            .filter(nombre -> !nombre.isBlank())
            .limit(2)
            .collect(Collectors.toList());

        for (int i = 2; i < partes.length; i++) {
            String nombreManual = partes[i].trim();
            if (!nombreManual.isEmpty() && !nombresTalleristas.contains(nombreManual)) {
                nombresTalleristas.add(nombreManual);
            }
        }

        nombresTalleristas = nombresTalleristas.stream().limit(2).toList();

        String nuevoTitulo = colegio.getNombre() + " - " + taller.getNombre();
        if (!nombresTalleristas.isEmpty()) {
            nuevoTitulo += " - " + String.join(" - ", nombresTalleristas);
        }
        entry.setTitle(nuevoTitulo);

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

private Entry<Agenda> showNewAgendaDialog(LocalDate date, Agenda agendaExistente) {
    try {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Nueva Agenda");
        dialog.setHeaderText(agendaExistente == null ? "Crear nueva visita" : "Editar visita");
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        ComboBox<Taller> comboTaller = new ComboBox<>();
        comboTaller.getItems().addAll(talleres);
        if (agendaExistente != null) comboTaller.setValue(agendaExistente.getTaller());

        ComboBox<Colegio> comboColegio = new ComboBox<>();
        comboColegio.getItems().addAll(colegios);
        comboColegio.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(Colegio item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.getNombre());
            }
        });
        comboColegio.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(Colegio item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.getNombre());
            }
        });
        if (agendaExistente != null) comboColegio.setValue(agendaExistente.getColegio());

        List<Usuario> talleristas = client.listUsuarios().stream()
                .filter(u -> "ROLE_TALLERISTA".equalsIgnoreCase(u.getRol()))
                .toList();

        ComboBox<Usuario> comboTallerista1 = new ComboBox<>();
        comboTallerista1.getItems().addAll(talleristas);
        ComboBox<Usuario> comboTallerista2 = new ComboBox<>();
        comboTallerista2.getItems().addAll(talleristas);

        for (ComboBox<Usuario> cb : List.of(comboTallerista1, comboTallerista2)) {
            cb.setCellFactory(lv -> new ListCell<>() {
                @Override
                protected void updateItem(Usuario item, boolean empty) {
                    super.updateItem(item, empty);
                    setText(empty || item == null ? null : item.getNombre());
                }
            });
            cb.setButtonCell(new ListCell<>() {
                @Override
                protected void updateItem(Usuario item, boolean empty) {
                    super.updateItem(item, empty);
                    setText(empty || item == null ? null : item.getNombre());
                }
            });
        }

        if (agendaExistente != null) {
            List<Usuario> asignados = client.listTalleristasPorAgenda(agendaExistente.getId());
            if (asignados.size() > 0) comboTallerista1.setValue(asignados.get(0));
            if (asignados.size() > 1) comboTallerista2.setValue(asignados.get(1));
        }

        grid.add(new Label("Taller:"), 0, 0);
        grid.add(comboTaller, 1, 0);
        grid.add(new Label("Colegio:"), 0, 1);
        grid.add(comboColegio, 1, 1);
        grid.add(new Label("Tallerista 1:"), 0, 2);
        grid.add(comboTallerista1, 1, 2);
        grid.add(new Label("Tallerista 2:"), 0, 3);
        grid.add(comboTallerista2, 1, 3);

        dialog.getDialogPane().setContent(grid);

        final Entry<Agenda>[] newEntry = new Entry[1];

        dialog.showAndWait().ifPresent(result -> {
            if (result == ButtonType.OK) {
                Taller tallerSel = comboTaller.getValue();
                Colegio colegioSel = comboColegio.getValue();
                Usuario t1 = comboTallerista1.getValue();
                Usuario t2 = comboTallerista2.getValue();

                if (tallerSel == null || colegioSel == null) {
                    alertError("Campos incompletos", "Debes seleccionar taller y colegio");
                    return;
                }

                try {
                    Agenda agenda;
                    if (agendaExistente != null) {
                        agendaExistente.setTaller(tallerSel);
                        agendaExistente.setColegio(colegioSel);
                        agendaExistente.setFecha(date.toString());
                        agendaExistente.setHora("09:00:00");
                        client.actualizarAgenda(agendaExistente.getId(), Map.of(
                                "fecha", agendaExistente.getFecha(),
                                "hora", agendaExistente.getHora(),
                                "taller", Map.of("id", tallerSel.getId()),
                                "colegio", Map.of("id", colegioSel.getId())
                        ));

                        client.eliminarAsignacionesPorAgenda(agendaExistente.getId());
                        if (t1 != null) client.asignarTallerista(agendaExistente.getId(), t1.getId());
                        if (t2 != null) client.asignarTallerista(agendaExistente.getId(), t2.getId());

                        agenda = agendaExistente;
                    } else {
                        client.crearAgenda(Map.of(
                                "fecha", date.toString(),
                                "hora", "09:00:00",
                                "taller", Map.of("id", tallerSel.getId()),
                                "colegio", Map.of("id", colegioSel.getId())
                        ));
                        List<Agenda> agendas = client.listAgendas();
                        agenda = agendas.get(agendas.size() - 1);

                        if (t1 != null) client.asignarTallerista(agenda.getId(), t1.getId());
                        if (t2 != null) client.asignarTallerista(agenda.getId(), t2.getId());
                    }

                    List<String> nombresTalleristas = List.of(t1, t2).stream()
                            .filter(Objects::nonNull)
                            .map(Usuario::getNombre)
                            .limit(2)
                            .toList();

                    String titulo = colegioSel.getNombre() + " - " + tallerSel.getNombre();
                    if (!nombresTalleristas.isEmpty()) {
                        titulo += " - " + String.join(" - ", nombresTalleristas);
                    }

                    newEntry[0] = new Entry<>(titulo);
                    newEntry[0].setInterval(date.atTime(LocalTime.of(9, 0)), date.atTime(LocalTime.of(10, 0)));
                    newEntry[0].setUserObject(agenda);

                    loadCalendar();

                } catch (Exception e) {
                    alertError("Error guardando agenda", e);
                }
            }
        });

        return newEntry[0];

    } catch (Exception e) {
        alertError("Error mostrando diálogo", e);
        return null;
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

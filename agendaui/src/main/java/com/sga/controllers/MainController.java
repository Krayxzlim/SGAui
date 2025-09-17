package com.sga.controllers;

import com.sga.service.RESTClient;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.Node;

public class MainController {

    @FXML private TabPane tabPane;

    private static RESTClient client;
    private String userRole; // admin, supervisor, tallerista

    // Controllers de los módulos
    private ColegiosController colegiosController;
    private TalleresController talleresController;
    private UsuariosController usuariosController;
    private AgendaController agendaController;
    private ReportesController reportesController;

    public static void setClient(RESTClient c) {
        client = c;
    }

    public void setUserRole(String role) {
        this.userRole = role;
        applyPermissions();
    }

    private void applyPermissions() {
        switch (userRole) {
            case "admin":
                break;
            case "supervisor":
                getTabByText("Usuarios").setDisable(true);
                break;
            case "tallerista":
                getTabByText("Usuarios").setDisable(true);
                getTabByText("Reportes").setDisable(true);
                break;
        }
    }

    @FXML
    public void initialize() {
        try {
            // Colegios
            FXMLLoader fxmlColegios = new FXMLLoader(getClass().getResource("Colegios.fxml"));
            Node nodeColegios = fxmlColegios.load();
            colegiosController = fxmlColegios.getController();
            colegiosController.setClient(client);
            colegiosController.loadData();
            getTabByText("Colegios").setContent(nodeColegios);

            // Talleres
            FXMLLoader fxmlTalleres = new FXMLLoader(getClass().getResource("Talleres.fxml"));
            Node nodeTalleres = fxmlTalleres.load();
            talleresController = fxmlTalleres.getController();
            talleresController.setClient(client);
            talleresController.loadData();
            getTabByText("Talleres").setContent(nodeTalleres);

            // Usuarios
            FXMLLoader fxmlUsuarios = new FXMLLoader(getClass().getResource("Usuarios.fxml"));
            Node nodeUsuarios = fxmlUsuarios.load();
            usuariosController = fxmlUsuarios.getController();
            usuariosController.setClient(client);
            usuariosController.loadData();
            getTabByText("Usuarios").setContent(nodeUsuarios);

            // Agenda
            FXMLLoader fxmlAgenda = new FXMLLoader(getClass().getResource("Agenda.fxml"));
            Node nodeAgenda = fxmlAgenda.load();
            agendaController = fxmlAgenda.getController();
            agendaController.setClient(client);
            agendaController.loadData();
            getTabByText("Agenda").setContent(nodeAgenda);

            // Reportes
            FXMLLoader fxmlReportes = new FXMLLoader(getClass().getResource("Reportes.fxml"));
            Node nodeReportes = fxmlReportes.load();
            reportesController = fxmlReportes.getController();
            reportesController.setClient(client);
            reportesController.loadData();
            getTabByText("Reportes").setContent(nodeReportes);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private Tab getTabByText(String text) {
        return tabPane.getTabs().stream()
                .filter(tab -> tab.getText().equals(text))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Tab " + text + " no encontrado"));
    }
}

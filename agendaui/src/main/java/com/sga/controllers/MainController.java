package com.sga.controllers;

import java.net.URL;

import com.sga.model.Colegio;
import com.sga.model.Taller;
import com.sga.service.RESTClient;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;

public class MainController {

    @FXML private TabPane tabPane;

    private static RESTClient client = new RESTClient();
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
    
    private Tab getTabByText(String text) {
        for (Tab tab : tabPane.getTabs()) {
            if (tab.getText().equals(text)) {
                return tab;
            }
        }
        return null; // o lanzar excepción si no se encuentra
    }


    @FXML
    public void initialize() {
        try {
            // Crear listas compartidas
            ObservableList<Taller> talleresShared = FXCollections.observableArrayList();
            ObservableList<Colegio> colegiosShared = FXCollections.observableArrayList();

            // Cargar tabs y obtener los controladores
            colegiosController = (ColegiosController) loadTab("Colegios", "com/sga/Colegios.fxml");
            talleresController = (TalleresController) loadTab("Talleres", "com/sga/Talleres.fxml");
            usuariosController = (UsuariosController) loadTab("Usuarios", "com/sga/Usuarios.fxml");
            usuariosController.setClient(client);
            usuariosController.loadData();
            agendaController = (AgendaController) loadTab("Agenda", "com/sga/Agenda.fxml");
            reportesController = (ReportesController) loadTab("Reportes", "com/sga/Reportes.fxml");

            // Inyectar listas compartidas en los controladores
            colegiosController.setData(client, colegiosShared);
            talleresController.setData(client, talleresShared);
            agendaController.setData(client, talleresShared, colegiosShared);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private Object loadTab(String tabName, String fxmlPath) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/" + fxmlPath));
        Node node = loader.load();
        Object controller = loader.getController();
        getTabByText(tabName).setContent(node);
        return controller;
    }
}

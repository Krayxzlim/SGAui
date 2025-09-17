package com.sga.controllers;

import java.net.URL;

import com.sga.service.RESTClient;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;

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
            loadTab("Colegios", "com/sga/Colegios.fxml");
            loadTab("Talleres", "com/sga/Talleres.fxml");
            loadTab("Usuarios", "com/sga/Usuarios.fxml");
            loadTab("Agenda", "com/sga/Agenda.fxml");
            loadTab("Reportes", "com/sga/Reportes.fxml");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void loadTab(String tabName, String fxmlPath) throws Exception {
        URL url = getClass().getResource("/" + fxmlPath); // agrega "/" al inicio
        System.out.println("Cargando FXML desde: " + url);
        FXMLLoader loader = new FXMLLoader(url);
        Node node = loader.load();

        Object controller = loader.getController();
        if (controller instanceof ColegiosController cc) cc.setClient(client);
        else if (controller instanceof TalleresController tc) tc.setClient(client);
        else if (controller instanceof UsuariosController uc) uc.setClient(client);
        else if (controller instanceof AgendaController ac) ac.setClient(client);
        else if (controller instanceof ReportesController rc) rc.setClient(client);

        getTabByText(tabName).setContent(node);
    }

}

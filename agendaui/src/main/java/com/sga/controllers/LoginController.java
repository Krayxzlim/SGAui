package com.sga.controllers;

import com.sga.service.RESTClient;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class LoginController {
    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private Label errorLabel;

    private final RESTClient client = new RESTClient();

    @FXML
    private void handleLogin() {
        String email = emailField.getText();
        String pass = passwordField.getText();
        try {
            String token = client.login(email, pass);
            System.out.println("Login OK, token = " + token);

            // Pasar client al MainController
            MainController.setClient(client);

            // Cambiar escena
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/sga/Main.fxml"));
            Parent root = loader.load();

            Stage stage = (Stage) emailField.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Agenda - Principal");
            stage.show();

        } catch (Exception e) {
            errorLabel.setText("Error: " + e.getMessage());
        }
    }
}

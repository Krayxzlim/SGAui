package com.sga;

import com.sga.service.RESTClient;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

public class LoginController {
    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private Label errorLabel;

    private RESTClient client = new RESTClient();

    @FXML
    private void handleLogin() {
        String email = emailField.getText();
        String pass = passwordField.getText();
        try {
            String token = client.login(email, pass);
            System.out.println("Login OK, token = " + token);

            // Pasar client al siguiente controller
            MainController.setClient(client);

            // Cambiar escena
            App.setRoot("/fxml/main"); 
        } catch (Exception e) {
            errorLabel.setText("Error: " + e.getMessage());
        }
    }
}

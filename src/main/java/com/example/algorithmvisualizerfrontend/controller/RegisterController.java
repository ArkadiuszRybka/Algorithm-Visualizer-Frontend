package com.example.algorithmvisualizerfrontend.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import org.json.JSONObject;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Optional;

public class RegisterController {
    @FXML private TextField nameField;
    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private PasswordField repeatPasswordField;
    @FXML private Button cancelButton;
    @FXML private Button doneButton;

    @FXML
    private void initialize(){

    }

    @FXML
    private void handleCancelButton() {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/com/example/algorithmvisualizerfrontend/login-view.fxml"));
            Parent loginRoot = fxmlLoader.load();
            Stage currentStage = (Stage) cancelButton.getScene().getWindow();
            currentStage.setScene(new Scene(loginRoot));
            currentStage.setTitle("Login");
            currentStage.centerOnScreen();
            currentStage.show();
        } catch (Exception e) {
            showAlert("Error: "  + e.getMessage());
        }

    }

    @FXML
    private void handleDoneButton() {
        String name = nameField.getText();
        String email = emailField.getText();
        String password = passwordField.getText();
        String repeatPassword = repeatPasswordField.getText();
        Optional<String> validationError = validate(name, email, password, repeatPassword);
        if(validationError.isPresent()){
            showAlert(validationError.get());
            return;
        }

        try {
            HttpClient client = HttpClient.newHttpClient();
            String jsonBody = new JSONObject()
                    .put("name", name)
                    .put("email", email)
                    .put("password", password)
                    .toString();

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("http://localhost:8080/auth/register"))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if(response.statusCode() == 201 || response.statusCode() == 200){
                showAlert("Succesfully registered. You can log in now.");
                FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/com/example/algorithmvisualizerfrontend/login-view.fxml"));
                Parent loginRoot = fxmlLoader.load();
                Stage currentStage = (Stage) doneButton.getScene().getWindow();
                currentStage.setScene(new Scene(loginRoot));
                currentStage.setTitle("Login");
                currentStage.centerOnScreen();
                currentStage.show();
            } else if(response.statusCode() == 403) {
                showAlert("Email already in use");
            } else {
                showAlert("Registration failed " + response.statusCode());
            }
        } catch (Exception e) {
            showAlert("Error during register: " + e.getMessage());
        }

    }

    private Optional<String> validate(String name, String email, String pass, String repeatPass){
        if(name == null || name.trim().isEmpty() ||
           email == null || email.trim().isEmpty() ||
           pass == null || pass.trim().isEmpty() ||
           repeatPass == null || repeatPass.trim().isEmpty()) return Optional.of("Any field cannot be empty");
        if(!isValidEmail(email)) return Optional.of("Invalid email format");
        if(pass.length() < 8) return Optional.of("Password must be at least 8 characters ");
        if(!pass.equals(repeatPass)) return Optional.of("Passwords do not match");
        return Optional.empty();
    }

    private boolean isValidEmail(String login){
        return login.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$");
    }

    private void showAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Info");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}

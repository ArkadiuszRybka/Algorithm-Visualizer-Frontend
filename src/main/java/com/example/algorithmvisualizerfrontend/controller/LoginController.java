package com.example.algorithmvisualizerfrontend.controller;

import com.example.algorithmvisualizerfrontend.util.SessionContext;
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

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Optional;

public class LoginController {
    @FXML private TextField loginField;
    @FXML private PasswordField passwordField;
    @FXML private Button registerButton;

    public void initialize(){
        loginField.setText("zbyszek@gmail.com");
        passwordField.setText("tajnehaslo");
    }

    @FXML
    private void handleLogin(){
        String login = loginField.getText();
        String password = passwordField.getText();

        Optional<String> validationError = validate(login, password);
        if(validationError.isPresent()){
            showAlert(validationError.get());
            return;
        }

        try{
            HttpClient client = HttpClient.newHttpClient();
            String jsonBody = new JSONObject()
                    .put("email", login)
                    .put("password", password)
                    .toString();

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("http://localhost:8080/auth/login"))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if(response.statusCode() == 200){
                JSONObject json = new JSONObject(response.body());
                String token = json.getString("token");
                SessionContext.setToken(token);
                switchToDashboard();
            } else{
                showAlert("Login failed: " + response.body());
            }

        } catch (Exception e) {
            showAlert("Error during login: " + e.getMessage());
        }
    }

    private Optional<String> validate(String login, String password){
        if(login == null || login.trim().isEmpty()) return Optional.of("Email cannot be empty");
        if(!isValidEmail(login)) return Optional.of("Invalid email format");
        if(password == null || password.trim().isEmpty()) return Optional.of("Password cannot be empty");
        return Optional.empty();
    }

    private boolean isValidEmail(String login){
        return login.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$");
    }

    @FXML
    private void handleRegister() {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/com/example/algorithmvisualizerfrontend/register-view.fxml"));
            Parent registerRoot = fxmlLoader.load();
            Stage currentStage = (Stage) registerButton.getScene().getWindow();
            currentStage.setScene(new Scene(registerRoot));
            currentStage.setTitle("Register");
            currentStage.centerOnScreen();
            currentStage.show();
        } catch (Exception e) {
            showAlert("Error: " + e.getMessage());
        }

    }

    private void switchToDashboard(){
        try{
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/com/example/algorithmvisualizerfrontend/dashboard-view.fxml"));
            Parent dashboardRoot = fxmlLoader.load();
            Stage currentStage = (Stage) loginField.getScene().getWindow();
            currentStage.setScene(new Scene(dashboardRoot));
            currentStage.setTitle("Dashboard");
            currentStage.centerOnScreen();
            currentStage.show();
        } catch (Exception e){
            showAlert("Could not load dashboard: " + e.getMessage());
        }
    }

    private void showAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Login Info");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}

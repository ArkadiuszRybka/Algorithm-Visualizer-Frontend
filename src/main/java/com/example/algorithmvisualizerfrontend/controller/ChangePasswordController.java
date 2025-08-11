package com.example.algorithmvisualizerfrontend.controller;

import com.example.algorithmvisualizerfrontend.util.SessionContext;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.stage.Stage;
import org.json.JSONObject;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Optional;

public class ChangePasswordController {
    @FXML private PasswordField oldPasswordField;
    @FXML private PasswordField newPasswordField;
    @FXML private PasswordField repeatPasswordField;
    @FXML private Button cancelButton;
    @FXML private Button doneButton;

    @FXML
    private void handleDoneButton() {
        String oldPass = oldPasswordField.getText();
        String newPass = newPasswordField.getText();
        String repeatPass = repeatPasswordField.getText();

        Optional<String> validationError = validate(oldPass, newPass, repeatPass);
        if(validationError.isPresent()){
            showAlert(validationError.get());
            return;
        }

        try {
            HttpClient client = HttpClient.newHttpClient();
            String jsonBody = new JSONObject()
                    .put("oldPassword", oldPass)
                    .put("newPassword", newPass)
                    .toString();

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("http://localhost:8080/user"))
                    .header("Authorization", "Bearer " + SessionContext.getToken())
                    .header("Content-Type", "application/json")
                    .method("PATCH", HttpRequest.BodyPublishers.ofString(jsonBody))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if(response.statusCode() == 200){
                showAlert("Password changed succesfully. Please log in again");
                switchToLoginView();
            } else {
                showAlert("Failed to change password: " + response.body());
            }

        } catch (Exception e){
            showAlert("Error during changing password: " + e.getMessage());
        }
    }

    private Optional<String> validate(String oldPass, String newPass, String repeatPass){
        if(oldPass == null || oldPass.trim().isEmpty() ||
           newPass == null || newPass.trim().isEmpty() ||
           repeatPass == null || repeatPass.trim().isEmpty()) return Optional.of("Any field cannot be empty");
        if(!repeatPass.equals(newPass)) return Optional.of("Passwords do not match");
        if(newPass.length() < 8) return Optional.of("New password must be at least 8 characters");
        if(newPass.equals(oldPass)) return Optional.of("New password must be different from the old one");
        return Optional.empty();
    }

    @FXML
    private void handleCancelButton() {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/com/example/algorithmvisualizerfrontend/dashboard-view.fxml"));
            Parent dashboardRoot = fxmlLoader.load();
            Stage currentStage = (Stage) cancelButton.getScene().getWindow();
            currentStage.setScene(new Scene(dashboardRoot));
            currentStage.setTitle("Dashboard");
            currentStage.centerOnScreen();
            currentStage.show();
        } catch (Exception e){
            showAlert("Could not load dashboard: " + e.getMessage());
        }
    }

    private void switchToLoginView(){
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/com/example/algorithmvisualizerfrontend/login-view.fxml"));
            Parent loginRoot = fxmlLoader.load();
            Stage currentStage = (Stage) doneButton.getScene().getWindow();
            currentStage.setScene(new Scene(loginRoot));
            currentStage.setTitle("Login");
            currentStage.centerOnScreen();
            currentStage.show();
        } catch (Exception e) {
            showAlert("Error: " + e.getMessage());
        }

    }

    private void showAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Info");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}

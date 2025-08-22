package com.example.algorithmvisualizerfrontend.controller;

import com.example.algorithmvisualizerfrontend.model.AlgorithmDto;
import com.example.algorithmvisualizerfrontend.model.ProgressSummaryDto;
import com.example.algorithmvisualizerfrontend.model.QuizQuestionDto;
import com.example.algorithmvisualizerfrontend.util.SessionContext;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DashboardController {
    @FXML private ComboBox<AlgorithmDto> algorithmComboBox;
    @FXML private TextField inputField;
    @FXML private Button generateButton;
    @FXML private Button animationButton;
    @FXML private Button quizButton;
    @FXML private ProgressBar progressBar;
    @FXML private Label progressText;
    @FXML private ListView<String> quizStatusList;
    @FXML private Button logoutButton;
    @FXML private Button changePasswordButton;
    @FXML private Button deleteAccountButton;
    @FXML private Tooltip algorithmTooltip;

    private static final String BASE_URL = "http://localhost:8080";
    private static final ObjectMapper MAPPER = new ObjectMapper();
    private final HttpClient http = HttpClient.newHttpClient();

    @FXML
    public void initialize() {
        if(!SessionContext.isAuthenticated()){
            showInfo("Invalid token");
            return;
        }
        validateInputField();
        setupComboBoxAndQuizStatus();
        bindTooltipToComboBox();
        loadAlgorithms();
        fetchProgressSummary()
                .thenAccept(summary -> Platform.runLater(() -> updateProgressUI(summary)))
                .exceptionally(ex -> {
                   ex.printStackTrace();
                   Platform.runLater(() -> showError("Could not fetch progress summary: " + ex.getMessage()));
                   return null;
                });

    }

    private void validateInputField(){
        inputField.textProperty().addListener((obs, oldValue, newValue) -> {
            if (newValue == null) return;

            String s = newValue.replaceAll("[^0-9,]", "");
            boolean endsWithComma = s.endsWith(",");

            Matcher m = Pattern.compile("\\d{1,2}").matcher(s);
            List<String> tokens = new ArrayList<>();
            while (m.find()) {
                if (tokens.size() < 16) {
                    tokens.add(m.group());
                }
            }

            StringBuilder rebuilt = new StringBuilder(String.join(", ", tokens));

            if (endsWithComma && !tokens.isEmpty() && tokens.size() < 16) {
                rebuilt.append(", ");
            }

            String cleaned = rebuilt.toString();

            if (!cleaned.equals(newValue)) {
                inputField.setText(cleaned);
                inputField.positionCaret(cleaned.length());
            }
        });
    }

    private List<AlgorithmDto> fetchAlgorithms() {
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/algorithms"))
                .header("Authorization", "Bearer " + SessionContext.getToken())
                .GET()
                .build();

        try {
            HttpResponse<String> res = http.send(req, HttpResponse.BodyHandlers.ofString());
            if (res.statusCode() != 200) {
                throw new IOException("HTTP " + res.statusCode() + " -> " + res.body());
            }
            return MAPPER.readValue(res.body(), new TypeReference<List<AlgorithmDto>>() {});
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void setupComboBoxAndQuizStatus(){
        algorithmComboBox.setCellFactory(cb -> new ListCell<>() {
            @Override protected void updateItem(AlgorithmDto item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.getId() + ". " + item.getName());
            }
        });
        algorithmComboBox.setButtonCell(new ListCell<>() {
            @Override protected void updateItem(AlgorithmDto item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.getId() + ". " + item.getName());
            }
        });
        quizStatusList.setCellFactory(lv -> new ListCell<>() {
            @Override protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty ? null : item);
                setStyle("-fx-font-family: 'Consolas'; -fx-font-size: 11px;");
            }
        });
    }

    private void bindTooltipToComboBox() {
        algorithmComboBox.getSelectionModel().selectedItemProperty().addListener((obs, oldV, newV) ->{
            String description = (newV != null && newV.getDescription() != null && !newV.getDescription().isBlank())
                    ? newV.getDescription() : "No description available";
            algorithmTooltip.setText(description);
        });
    }

    private void loadAlgorithms(){
        CompletableFuture
                .supplyAsync(this::fetchAlgorithms) // wątku w tle
                .thenAccept(list -> Platform.runLater(() -> {
                    algorithmComboBox.getItems().setAll(list);
                    if (!list.isEmpty()) {
                        algorithmComboBox.getSelectionModel().selectFirst();
                    }
                }))
                .exceptionally(ex -> {
                    ex.printStackTrace();
                    Platform.runLater(() -> showError("Nie udało się pobrać algorytmów:\n" + ex.getMessage()));
                    return null;
                });
    }

    private CompletableFuture<ProgressSummaryDto> fetchProgressSummary() {
        return CompletableFuture.supplyAsync(() -> {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL + "/summary"))
                    .header("Authorization", "Bearer " + SessionContext.getToken())
                    .GET()
                    .build();
            try {
                HttpResponse<String> response = http.send(request, HttpResponse.BodyHandlers.ofString());
                if(response.statusCode() != 200) {
                    throw new IOException("Http " + response.statusCode() + " -> " + response.body());
                }
                return MAPPER.readValue(response.body(), ProgressSummaryDto.class);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }

    private void updateProgressUI(ProgressSummaryDto dto) {
        int total = dto.getTotalAlgorithms();
        long completed = dto.getCompletedCount();

        double progress = (total == 0) ? 0.0 : (double) completed / total;
        progressBar.setProgress(progress);
        if(progressText != null) {
            progressText.setText(completed + "/" + total);
        }

        quizStatusList.getItems().setAll(
                dto.getItems().stream()
                        .map(i -> i.getAlgorithmId() + ". " + i.getAlgorithmName() + " - " + i.getStatus())
                        .toList()
        );
    }

    @FXML
    private void handleLogout(){
        SessionContext.clear();
        showInfo("Logout");
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/com/example/algorithmvisualizerfrontend/login-view.fxml"));
            Parent loginRoot = fxmlLoader.load();
            Stage currentStage = (Stage) logoutButton.getScene().getWindow();
            currentStage.setScene(new Scene(loginRoot));
            currentStage.setTitle("Login");
            currentStage.centerOnScreen();
            currentStage.show();
        } catch (Exception e){
            showError("Logout error: " + e.getMessage());
        }
    }

    @FXML
    private void handleGenerate() {
        Random random = new Random();
        int amount = random.nextInt(16-8+1) + 8;

        StringBuilder builder = new StringBuilder();

        for(int i=0; i<amount; i++){
            builder.append(random.nextInt(99)+1);
            if(i < amount-1) builder.append(", ");
        }
        inputField.setText(builder.toString());
    }

    @FXML
    private void openDataEditor() {
        Dialog<String> dialog = new Dialog<>();
        dialog.setTitle("Edit data");
        dialog.setResizable(true);

        TextArea editor = new TextArea(inputField.getText());
        editor.setPrefRowCount(15);
        editor.setPrefColumnCount(60);
        editor.setWrapText(false);

        dialog.getDialogPane().setContent(editor);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        dialog.getDialogPane().setMinWidth(600);
        dialog.getDialogPane().setMinHeight(400);

        dialog.setResultConverter(bt -> bt == ButtonType.OK ? editor.getText() : null);
        dialog.showAndWait().ifPresent(text -> inputField.setText(text));
    }

    private void handleStartAnimation() {
        AlgorithmDto selected = algorithmComboBox.getValue();
        if (selected == null) {
            showInfo("Wybierz algorytm z listy.");
            return;
        }
        // TODO: przejście do widoku animacji i przekazanie selected, inputField.getText()
        showInfo("Start animacji: " + selected.getName());
    }

    @FXML
    private void handleStartQuiz() {
        AlgorithmDto selected = algorithmComboBox.getValue();
        if (selected == null) {
            showInfo("Wybierz algorytm z listy.");
            return;
        }

        List<QuizQuestionDto> questions = List.of();
        try {
            questions = fetchQuizQuestions(selected.getId());
        } catch (Exception e){
            showError("Could not load quiz " + e.getMessage());
            return;
        }

        openQuizWindow(selected.getName(), selected.getId(), questions);
    }

    private List<QuizQuestionDto> fetchQuizQuestions(Long algorithmId) throws Exception{
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/quiz/"+algorithmId))
                .header("Authorization", "Bearer " + SessionContext.getToken())
                .GET()
                .build();

        HttpResponse<String> response = http.send(request, HttpResponse.BodyHandlers.ofString());
        if(response.statusCode() != 200){
            showError("Http " + response.statusCode() + " " + response.body());
        }

        return MAPPER.readValue(response.body(), new TypeReference<List<QuizQuestionDto>>() {});
    }

    private void openQuizWindow(String algorithmName, long algorithmId, List<QuizQuestionDto> questions){
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/com/example/algorithmvisualizerfrontend/quiz-view.fxml"));
            Parent quizRoot = fxmlLoader.load();

            QuizController quizController = fxmlLoader.getController();
            quizController.setup(algorithmName, algorithmId, questions, this::refreshAfterQuiz);

            Stage stage = new Stage();
            stage.setTitle(algorithmName + " Quiz");
            stage.setScene(new Scene(quizRoot));
            stage.centerOnScreen();
            stage.initOwner(animationButton.getScene().getWindow());
            stage.initModality(Modality.WINDOW_MODAL);
            stage.setResizable(true);

            stage.showAndWait();
        }catch (Exception e) {
            showError("Could not load quizes " + e.getMessage());
        }
    }

    private void refreshAfterQuiz() {
        fetchProgressSummary()
                .thenAccept(summary -> Platform.runLater(() -> updateProgressUI(summary)))
                .exceptionally(ex -> {
                    Platform.runLater(() -> showError("Could not refresh progress: " + ex.getMessage()));
                    return null;
                });
    }

    @FXML
    private void handleChangePassword() {
        try{
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/com/example/algorithmvisualizerfrontend/changePass-view.fxml"));
            Parent changePassRoot = fxmlLoader.load();
            Stage currentStage = (Stage) changePasswordButton.getScene().getWindow();
            currentStage.setScene(new Scene(changePassRoot));
            currentStage.setTitle("Change password");
            currentStage.centerOnScreen();
            currentStage.show();
        } catch (Exception e){
            showError("Error: " + e.getMessage());
        }
    }

    private void handleDeleteAccount() {
        // TODO: wołanie DELETE /user z tokenem + powrót do logowania
        showInfo("Usunięcie konta — w przygotowaniu.");
    }

    private void showInfo(String msg) {
        Alert a = new Alert(Alert.AlertType.INFORMATION);
        a.setTitle("Info");
        a.setHeaderText(null);
        a.setContentText(msg);
        a.showAndWait();
    }

    private void showError(String msg) {
        Alert a = new Alert(Alert.AlertType.ERROR);
        a.setTitle("Error");
        a.setHeaderText(null);
        a.setContentText(msg);
        a.showAndWait();
    }
}

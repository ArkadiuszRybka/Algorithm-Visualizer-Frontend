package com.example.algorithmvisualizerfrontend.controller;

import com.example.algorithmvisualizerfrontend.model.*;
import com.example.algorithmvisualizerfrontend.util.SessionContext;
import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import lombok.Setter;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class QuizController {

    @FXML private Label quizTitle;
    @FXML private VBox quizContainer;
    @FXML private Button doneButton;
    @FXML private Button closeButton;

    private long algorithmId;
    private List<QuizQuestionDto> questions;

    private final Map<Long, ToggleGroup> groupsByQuestionId = new HashMap<>();
    private static final ObjectMapper MAPPER = new ObjectMapper();
    private final Map<Long, Integer> selectedAnswers = new HashMap<>();

    @Setter
    private Runnable onFinish;

    public void setup(String algorithmName, long algorithmId, List<QuizQuestionDto> questions, Runnable onFinish) {
        this.algorithmId = algorithmId;
        this.questions = questions;
        quizTitle.setText(algorithmName + " Quiz");
        this.onFinish = onFinish;
        renderQuestions();
    }

    private void renderQuestions() {
        quizContainer.getChildren().clear();
        groupsByQuestionId.clear();

        for (QuizQuestionDto q : questions) {
            Label qLabel = new Label(q.getQuestion());
            qLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 13px;");

            VBox box = new VBox(6);
            box.getChildren().add(qLabel);

            ToggleGroup group = new ToggleGroup();
            groupsByQuestionId.put(q.getId(), group);

            for (QuizAnswerDto a : q.getAnswers()) {
                RadioButton rb = new RadioButton(a.getText());
                rb.setToggleGroup(group);
                rb.setUserData(a.getId()); // KLUCZ: prawdziwe ID odpowiedzi z backendu
                box.getChildren().add(rb);
            }
            box.setStyle("-fx-padding:10; -fx-border-color:#ddd; -fx-border-radius:6;");
            quizContainer.getChildren().add(box);
        }
    }


    @FXML
    public void handleCancel() {
        Stage stage = (Stage) quizContainer.getScene().getWindow();
        stage.close();
    }

    @FXML
    private void handleDone() {
        List<QuizSelectionDto> selections = new ArrayList<>();

        for(var entry : groupsByQuestionId.entrySet()){
            Long quizId = entry.getKey();
            Toggle selected = entry.getValue().getSelectedToggle();
            if(selected == null) {
                showInfo("Please answer all questions");
                return;
            }
            Long answerId = (Long) selected.getUserData();
            selections.add(new QuizSelectionDto(quizId,answerId));
        }

        try {
            String body = MAPPER.writeValueAsString(new QuizSubmitRequest(selections));

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("http://localhost:8080/quiz/" + algorithmId + "/submit"))
                    .header("Authorization", "Bearer " + SessionContext.getToken())
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(body))
                    .build();

            HttpResponse<String> response = HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());

            System.out.println("status " + response.statusCode());
            System.out.println("body " + response.body());

            if(response.statusCode() == 200){
                QuizResultDto result = MAPPER.readValue(response.body(), QuizResultDto.class);
                showInfo("Result: " + result.getCorrectAnswers() + "/" + result.getTotalQuestions());
                if(onFinish !=null) onFinish.run();
                handleCancel();
            } else {
                showError("Sumbit failed: Http " + response.statusCode() + response.body());
            }
        } catch (Exception e) {
            showError("Submit error " + e.getMessage());
        }

    }

    private void showInfo(String msg) {
        Alert a = new Alert(Alert.AlertType.INFORMATION);
        a.setTitle("Info"); a.setHeaderText(null); a.setContentText(msg); a.showAndWait();
    }
    private void showError(String msg) {
        Alert a = new Alert(Alert.AlertType.ERROR);
        a.setTitle("Error"); a.setHeaderText(null); a.setContentText(msg); a.showAndWait();
    }
    
}



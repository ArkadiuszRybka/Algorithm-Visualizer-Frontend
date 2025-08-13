package com.example.algorithmvisualizerfrontend.controller;

import com.example.algorithmvisualizerfrontend.model.QuizAnswerDto;
import com.example.algorithmvisualizerfrontend.model.QuizQuestionDto;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class QuizController {

    @FXML private Label quizTitle;
    @FXML private VBox quizContainer;

    private long algorithmId;
    private List<QuizQuestionDto> questions;

    private final Map<Long, Integer> selectedAnswers = new HashMap<>();

    public void setup(String algorithmName, long algorithmId, List<QuizQuestionDto> questions) {
        this.algorithmId = algorithmId;
        this.questions = questions;
        quizTitle.setText(algorithmName + " Quiz");

        renderQuestions();
    }

    private void renderQuestions() {
        quizContainer.getChildren().clear();

        for (QuizQuestionDto q : questions) {
            Label questionLabel = new Label(q.getQuestion());
            questionLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");

            ToggleGroup group = new ToggleGroup();

            VBox answerBox = new VBox(5);
            for (QuizAnswerDto a : q.getAnswers()) {
                RadioButton rb = new RadioButton(a.getText());
                rb.setUserData(a.getId());   // do POST wyślesz answerId
                rb.setToggleGroup(group);
                answerBox.getChildren().add(rb);
            }

            VBox questionBox = new VBox(5, questionLabel, answerBox);
            questionBox.setStyle("-fx-padding: 10; -fx-border-color: lightgray; -fx-border-radius: 5;");
            quizContainer.getChildren().add(questionBox);
        }
    }


    @FXML
    public void handleCancel() {
        Stage stage = (Stage) quizContainer.getScene().getWindow();
        stage.close();
    }

    @FXML
    private void handleDone() {
        // Tu można później wysłać POST z odpowiedziami
        System.out.println("Wybrane odpowiedzi: " + selectedAnswers);

        Alert alert = new Alert(Alert.AlertType.INFORMATION, "Quiz completed!", ButtonType.OK);
        alert.showAndWait();

        handleCancel();
    }

    public static class AnswerSelection {
        public final Long quizId;
        public final Long answerId;
        public AnswerSelection(Long quizId, Long answerId) {
            this.quizId = quizId;
            this.answerId = answerId;
        }
    }
}



package com.example.algorithmvisualizerfrontend.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data @NoArgsConstructor @AllArgsConstructor
public class QuizQuestionDto {
    private Long id;
    private String question;
    private List<QuizAnswerDto> answers;
}

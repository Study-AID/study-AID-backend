package com.example.api.service.dto.quiz;

import com.example.api.entity.QuizItem;
import com.example.api.entity.enums.QuestionType;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class QuizItemOutput {
    private UUID id;
    private UUID quizId;
    private UUID userId;
    private String question;
    private QuestionType questionType;
    private String explanation;
    private Boolean isTrueAnswer;
    private String[] choices;
    private Integer[] answerIndices;
    private String textAnswer;
    private Integer displayOrder;
    private Float points;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static QuizItemOutput fromEntity(QuizItem quizItem) {
        return new QuizItemOutput(
                quizItem.getId(),
                quizItem.getQuiz().getId(),
                quizItem.getUser().getId(),
                quizItem.getQuestion(),
                quizItem.getQuestionType(),
                quizItem.getExplanation(),
                quizItem.getIsTrueAnswer(),
                quizItem.getChoices(),
                quizItem.getAnswerIndices(),
                quizItem.getTextAnswer(),
                quizItem.getDisplayOrder(),
                quizItem.getPoints(),
                quizItem.getCreatedAt(),
                quizItem.getUpdatedAt()
        );
    }
}

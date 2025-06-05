package com.example.api.controller.dto.quiz;

import java.time.LocalDateTime;
import java.util.UUID;

import com.example.api.entity.QuizItem;
import com.example.api.entity.enums.QuestionType;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Quiz item response DTO")
public class QuizItemResponse {
    @Schema(description = "Quiz item ID")
    private UUID id;

    @Schema(description = "Question text")
    private String question;

    @Schema(description = "Question type")
    private QuestionType questionType;

    @Schema(description = "Explanation")
    private String explanation;

    @Schema(description = "True/False answer for true_or_false type")
    private Boolean isTrueAnswer;

    @Schema(description = "Multiple choice options")
    private String[] choices;

    @Schema(description = "Correct answer indices for multiple choice")
    private Integer[] answerIndices;

    @Schema(description = "Text answer for short_answer/essay type")
    private String textAnswer;

    @Schema(description = "Display order")
    private Integer displayOrder;

    @Schema(description = "Points")
    private Float points;

    @Schema(description = "Is liked")
    private Boolean isLiked;

    @Schema(description = "Creation time")
    private LocalDateTime createdAt;

    @Schema(description = "Last update time")
    private LocalDateTime updatedAt;

    public static QuizItemResponse fromEntity(QuizItem quizItem) {
        return new QuizItemResponse(
                quizItem.getId(),
                quizItem.getQuestion(),
                quizItem.getQuestionType(),
                quizItem.getExplanation(),
                quizItem.getIsTrueAnswer(),
                quizItem.getChoices(),
                quizItem.getAnswerIndices(),
                quizItem.getTextAnswer(),
                quizItem.getDisplayOrder(),
                quizItem.getPoints(),
                quizItem.getIsLiked(),
                quizItem.getCreatedAt(),
                quizItem.getUpdatedAt()
        );
    }
}

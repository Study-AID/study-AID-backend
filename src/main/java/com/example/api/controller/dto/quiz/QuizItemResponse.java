package com.example.api.controller.dto.quiz;

import java.time.LocalDateTime;
import java.util.UUID;

import com.example.api.entity.QuizItem;
import com.example.api.entity.enums.QuestionType;
import com.example.api.service.dto.quiz.QuizItemOutput;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Quiz item response DTO")
public class QuizItemResponse {
    @NotNull
    @Schema(description = "Quiz item ID")
    private UUID id;

    @NotNull
    @Schema(description = "Question text")
    private String question;

    @NotNull
    @Schema(description = "Question type")
    private QuestionType questionType;

    @NotNull
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

    @NotNull
    @Schema(description = "Creation time")
    private LocalDateTime createdAt;

    @NotNull
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

    public static QuizItemResponse fromServiceDto(QuizItemOutput quizItemOutput) {
        return new QuizItemResponse(
                quizItemOutput.getId(),
                quizItemOutput.getQuestion(),
                quizItemOutput.getQuestionType(),
                quizItemOutput.getExplanation(),
                quizItemOutput.getIsTrueAnswer(),
                quizItemOutput.getChoices(),
                quizItemOutput.getAnswerIndices(),
                quizItemOutput.getTextAnswer(),
                quizItemOutput.getDisplayOrder(),
                quizItemOutput.getPoints(),
                quizItemOutput.getIsLiked(),
                quizItemOutput.getCreatedAt(),
                quizItemOutput.getUpdatedAt()
        );
    }
}

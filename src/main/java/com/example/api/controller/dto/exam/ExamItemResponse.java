package com.example.api.controller.dto.exam;

import java.time.LocalDateTime;
import java.util.UUID;

import com.example.api.entity.ExamItem;
import com.example.api.entity.enums.QuestionType;
import com.example.api.service.dto.exam.ExamItemOutput;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Exam item response DTO")
public class ExamItemResponse {
    @NotNull
    @Schema(description = "Exam item ID")
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

    public static ExamItemResponse fromEntity(ExamItem examItem) {
        return new ExamItemResponse(
                examItem.getId(),
                examItem.getQuestion(),
                examItem.getQuestionType(),
                examItem.getExplanation(),
                examItem.getIsTrueAnswer(),
                examItem.getChoices(),
                examItem.getAnswerIndices(),
                examItem.getTextAnswer(),
                examItem.getDisplayOrder(),
                examItem.getPoints(),
                examItem.getIsLiked(),
                examItem.getCreatedAt(),
                examItem.getUpdatedAt()
        );
    }
    public static ExamItemResponse fromServiceDto(ExamItemOutput examItemOutput) {
        return new ExamItemResponse(
                examItemOutput.getId(),
                examItemOutput.getQuestion(),
                examItemOutput.getQuestionType(),
                examItemOutput.getExplanation(),
                examItemOutput.getIsTrueAnswer(),
                examItemOutput.getChoices(),
                examItemOutput.getAnswerIndices(),
                examItemOutput.getTextAnswer(),
                examItemOutput.getDisplayOrder(),
                examItemOutput.getPoints(),
                examItemOutput.getIsLiked(),
                examItemOutput.getCreatedAt(),
                examItemOutput.getUpdatedAt()
        );
    }
}

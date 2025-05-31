package com.example.api.controller.dto.exam;

import java.time.LocalDateTime;

import com.example.api.service.dto.exam.ExamItemOutput;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Exam Item response DTO")
public class ExamItemResponse {
    @Schema(description = "Exam Item ID")
    private String id;

    @Schema(description = "Exam ID")
    private String examId;

    @Schema(description = "User ID")
    private String userId;

    @Schema(description = "Question text")
    private String question;

    @Schema(description = "Question type")
    private String questionType;

    @Schema(description = "Explanation for the question")
    private String explanation;

    @Schema(description = "Is the answer true?")
    private Boolean isTrueAnswer;

    @Schema(description = "Choices for the question")
    private String[] choices;

    @Schema(description = "Indices of the correct answers")
    private Integer[] answerIndices;

    @Schema(description = "Text answer for the question")
    private String textAnswer;

    @Schema(description = "Display order of the question")
    private Integer displayOrder;

    @Schema(description = "Points for the question")
    private Float points;

    @Schema(description = "Is the question liked by the user")
    private Boolean isLiked;
    
    @Schema(description = "Creation time of the question")
    private LocalDateTime createdAt;
    
    @Schema(description = "Last update time of the question")
    private LocalDateTime updatedAt;

    public static ExamItemResponse fromServiceDto(ExamItemOutput examItem) {
        return new ExamItemResponse(
                examItem.getId().toString(),
                examItem.getExamId().toString(),
                examItem.getUserId().toString(),
                examItem.getQuestion(),
                examItem.getQuestionType().name(),
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
}

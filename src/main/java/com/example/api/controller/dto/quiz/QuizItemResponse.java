package com.example.api.controller.dto.quiz;

import java.time.LocalDateTime;
import java.util.UUID;

import com.example.api.entity.enums.QuestionType;
import com.example.api.service.dto.quiz.QuizItemOutput;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Quiz Item response DTO")
public class QuizItemResponse {
    @Schema(description = "Quiz Item ID")
    private UUID id;

    @Schema(description = "Quiz ID")
    private UUID quizId;

    @Schema(description = "User ID")
    private UUID userId;

    @Schema(description = "Question text")
    private String question;

    @Schema(description = "Question type")
    private QuestionType questionType;

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
    
    @Schema(description = "points for the question")
    private Float points;
    
    @Schema(description = "Creation time of the question")
    private LocalDateTime createdAt;
    
    @Schema(description = "Last update time of the question")
    private LocalDateTime updatedAt;

    public static QuizItemResponse fromServiceDto(QuizItemOutput quizItem) {
        return new QuizItemResponse(
                quizItem.getId(),
                quizItem.getQuizId(),
                quizItem.getUserId(),
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

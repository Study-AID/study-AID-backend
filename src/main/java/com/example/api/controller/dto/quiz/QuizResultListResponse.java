package com.example.api.controller.dto.quiz;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

import com.example.api.service.dto.quiz.QuizResultListOutput;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "List of quiz results response")
public class QuizResultListResponse {
    @Schema(description = "List of quiz results response")
    private List<QuizResultResponse> quizResults;

    private Float averageScore;

    public static QuizResultListResponse fromServiceDto(QuizResultListOutput quizResultListOutput, 
                                                       Float averageScore) {
        List<QuizResultResponse> quizResultResponses = quizResultListOutput.getQuizResults().stream()
                .map(QuizResultResponse::fromServiceDto)
                .toList();
        return new QuizResultListResponse(quizResultResponses, averageScore);
    }
}

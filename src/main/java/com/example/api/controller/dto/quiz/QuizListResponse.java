package com.example.api.controller.dto.quiz;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

import com.example.api.service.dto.quiz.QuizListOutput;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "List of quizzes response")
public class QuizListResponse {
    @Schema(description = "List of quizzes response")
    private List<QuizResponse> quizzes;

    public static QuizListResponse fromServiceDto(QuizListOutput quizListOutput) {
        return new QuizListResponse(
                quizListOutput.getQuizzes().stream()
                        .map(QuizResponse::fromServiceDto)
                        .toList()
        );
    }
}

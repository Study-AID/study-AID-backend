package com.example.api.controller.dto.quiz;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

import com.example.api.service.dto.quiz.QuizResponseListOutput;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "List of quiz responses response")
public class QuizResponseListResponse {
    @Schema(description = "List of quiz responses response")
    private List<QuizResponseResponse> quizResponses;

    public static QuizResponseListResponse fromServiceDto(QuizResponseListOutput quizResponseListOutput) {
        return new QuizResponseListResponse(
                quizResponseListOutput.getQuizResponses().stream()
                        .map(QuizResponseResponse::fromServiceDto)
                        .toList()
        );
    }
}

package com.example.api.controller.dto.quiz;

import java.util.List;

import com.example.api.entity.QuizResponse;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@Schema(description = "Quiz responses submission request")
public class SubmitQuizRequest {
    @NotBlank
    @Schema(description = "quiz responses")
    private List<QuizResponse> quizResponses;
}

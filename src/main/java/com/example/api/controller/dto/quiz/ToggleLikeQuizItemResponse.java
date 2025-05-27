package com.example.api.controller.dto.quiz;

import com.example.api.service.dto.quiz.ToggleLikeQuizItemOutput;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@Schema(description = "퀴즈 문제 좋아요 토글 응답")
public class ToggleLikeQuizItemResponse {
    @Schema(description = "퀴즈 ID")
    private UUID quizId;
    
    @Schema(description = "퀴즈 문제 ID")
    private UUID quizItemId;
    
    @Schema(description = "사용자 ID")
    private UUID userId;
    
    @Schema(description = "좋아요 상태 (true: 좋아요, false: 좋아요 취소)")
    private boolean isLiked;
    
    public ToggleLikeQuizItemResponse(UUID quizId, UUID quizItemId, UUID userId, boolean isLiked) {
        this.quizId = quizId;
        this.quizItemId = quizItemId;
        this.userId = userId;
        this.isLiked = isLiked;
    }
    
    /**
     * Service DTO에서 Controller DTO로 변환하는 정적 메서드
     */
    public static ToggleLikeQuizItemResponse fromServiceDto(ToggleLikeQuizItemOutput output) {
        return new ToggleLikeQuizItemResponse(
                output.getQuizId(),
                output.getQuizItemId(),
                output.getUserId(),
                output.isLiked()
        );
    }
}
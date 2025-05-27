package com.example.api.controller.dto.exam;

import com.example.api.service.dto.exam.ToggleLikeExamItemOutput;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@Schema(description = "시험 문제 좋아요 토글 응답")
public class ToggleLikeExamItemResponse {
    @Schema(description = "시험 ID")
    private UUID examId;

    @Schema(description = "시험 문제 ID")
    private UUID examItemId;

    @Schema(description = "사용자 ID")
    private UUID userId;

    @Schema(description = "좋아요 상태 (true: 좋아요, false: 좋아요 취소)")
    private boolean isLiked;

    public ToggleLikeExamItemResponse(UUID examId, UUID examItemId, UUID userId, boolean isLiked) {
        this.examId = examId;
        this.examItemId = examItemId;
        this.userId = userId;
        this.isLiked = isLiked;
    }

    /**
     * Service DTO에서 Controller DTO로 변환하는 정적 메서드
     */
    public static ToggleLikeExamItemResponse fromServiceDto(ToggleLikeExamItemOutput output) {
        return new ToggleLikeExamItemResponse(
                output.getExamId(),
                output.getExamItemId(),
                output.getUserId(),
                output.isLiked()
        );
    }
}

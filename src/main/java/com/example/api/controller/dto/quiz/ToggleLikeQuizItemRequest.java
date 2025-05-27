package com.example.api.controller.dto.quiz;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Schema(description = "퀴즈 문제 좋아요 토글 요청")
public class ToggleLikeQuizItemRequest {
    // 현재 토글 기능은 PATH 파라미터로 quizId와 quizItemId를 받으므로
    // Request Body에는 추가 필드가 필요하지 않습니다.
    // 확장성을 위해 클래스는 유지하되 현재는 빈 클래스로 구성합니다.
}
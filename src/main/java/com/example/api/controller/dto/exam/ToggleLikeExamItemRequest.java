package com.example.api.controller.dto.exam;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Schema(description = "시험 문제 좋아요 토글 요청")
public class ToggleLikeExamItemRequest {

}

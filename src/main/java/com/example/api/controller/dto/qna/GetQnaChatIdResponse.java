package com.example.api.controller.dto.qna;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.UUID;

@Getter
@AllArgsConstructor
@JsonIgnoreProperties({"vectorized"}) // Jackson이 JSON에 자동 생성하는 vectorized 필드는 무시
public class GetQnaChatIdResponse {
    @NotNull
    private UUID chatId;
    @NotNull

    @JsonProperty("isVectorized") // 해당 필드를 JSON 응답에 isVectorized로 표시하도록 명시
    private boolean isVectorized;
}
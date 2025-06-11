package com.example.api.controller.dto.exam;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

import com.example.api.entity.ExamItem;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "List of exam items response")
public class ExamItemListResponse {
    @Schema(description = "List of exam items")
    private List<ExamItemResponse> examItems;

    public static ExamItemListResponse fromEntities(List<ExamItem> examItems) {
        return new ExamItemListResponse(
                examItems.stream()
                        .map(ExamItemResponse::fromEntity)
                        .toList()
        );
    }
}

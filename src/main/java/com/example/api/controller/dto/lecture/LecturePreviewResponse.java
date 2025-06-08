package com.example.api.controller.dto.lecture;

import com.example.api.config.StorageConfig;
import com.example.api.controller.dto.lecture.summary.Keyword;
import com.example.api.controller.dto.lecture.summary.PageRange;
import com.example.api.service.dto.lecture.LectureOutput;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Lecture preview response DTO with keywords")
public class LecturePreviewResponse {
    @NotNull
    @Schema(description = "Unique ID of the lecture")
    private UUID id;

    @NotNull
    @Schema(description = "Title of the lecture")
    private String title;

    @Schema(description = "Keywords extracted from the lecture")
    private List<Keyword> keywords;

    /**
     * Extract keywords from summary map
     */
    private static List<Keyword> extractKeywords(Map<String, Object> summaryMap) {
        if (summaryMap == null) {
            return new ArrayList<>();
        }

        try {
            List<Map<String, Object>> keywordsList = (List<Map<String, Object>>) summaryMap.get("keywords");
            return convertToKeywords(keywordsList);
        } catch (Exception e) {
            // 변환 실패 시 빈 리스트 반환
            System.err.println("Keywords 추출 중 오류 발생: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    /**
     * List<Map>을 List<Keyword>로 변환
     */
    private static List<Keyword> convertToKeywords(List<Map<String, Object>> keywordsList) {
        if (keywordsList == null) {
            return new ArrayList<>();
        }

        List<Keyword> keywords = new ArrayList<>();
        for (Map<String, Object> keywordMap : keywordsList) {
            if (keywordMap != null) {
                Keyword keyword = convertToKeyword(keywordMap);
                if (keyword != null) {
                    keywords.add(keyword);
                }
            }
        }
        return keywords;
    }

    /**
     * Map을 Keyword 객체로 변환
     */
    private static Keyword convertToKeyword(Map<String, Object> keywordMap) {
        if (keywordMap == null) {
            return null;
        }

        String keyword = (String) keywordMap.get("keyword");
        String description = (String) keywordMap.get("description");

        // relevance는 Number로 받아서 float로 변환 (Integer나 Double일 수 있음)
        Number relevanceNum = (Number) keywordMap.get("relevance");
        float relevance = relevanceNum != null ? relevanceNum.floatValue() : 0.0f;

        PageRange pageRange = convertToPageRange((Map<String, Object>) keywordMap.get("page_range"));

        return new Keyword(keyword, description, relevance, pageRange);
    }

    /**
     * Map을 PageRange 객체로 변환
     */
    private static PageRange convertToPageRange(Map<String, Object> pageRangeMap) {
        if (pageRangeMap == null) {
            return null;
        }

        // start_page와 end_page는 Number로 받아서 int로 변환
        Number startPageNum = (Number) pageRangeMap.get("start_page");
        Number endPageNum = (Number) pageRangeMap.get("end_page");

        int startPage = startPageNum != null ? startPageNum.intValue() : 0;
        int endPage = endPageNum != null ? endPageNum.intValue() : 0;

        return new PageRange(startPage, endPage);
    }

    public static LecturePreviewResponse fromServiceDto(LectureOutput lecture, StorageConfig storageConfig) {
        return new LecturePreviewResponse(
                lecture.getId(),
                lecture.getTitle(),
                extractKeywords(lecture.getSummary())
        );
    }
}

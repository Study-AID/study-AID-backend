package com.example.api.controller.dto.lecture;

import com.example.api.config.StorageConfig;
import com.example.api.controller.dto.lecture.summary.Keyword;
import com.example.api.controller.dto.lecture.summary.Metadata;
import com.example.api.controller.dto.lecture.summary.PageRange;
import com.example.api.controller.dto.lecture.summary.Summary;
import com.example.api.controller.dto.lecture.summary.TopicDetails;
import com.example.api.entity.enums.SummaryStatus;
import com.example.api.entity.ParsedText;
import com.example.api.service.dto.lecture.LectureOutput;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Lecture response DTO")
public class LectureResponse {
    @NotNull
    @Schema(description = "Unique ID of the lecture")
    private UUID id;

    @NotNull
    @Schema(description = "Unique ID of the course")
    private UUID courseId;

    @NotNull
    @Schema(description = "Unique ID of the user")
    private UUID userId;

    @NotNull
    @Schema(description = "Title of the lecture")
    private String title;

    @Schema(description = "Full URL to the lecture material")
    private String materialUrl;

    @Schema(description = "Display order lexicographically")
    private String displayOrderLex;

    @Schema(description = "Parsed text from PDF")
    private ParsedText parsedText;

    @Schema(description = "Notes associated with the lecture")
    private Map<String, Object> note;

    @Schema(description = "Summary of the lecture")
    private Summary summary;

    @Schema(description = "Summary status of the lecture")
    private SummaryStatus summaryStatus;

    @NotNull
    @Schema(description = "Creation timestamp of the lecture")
    private LocalDateTime createdAt;

    @NotNull
    @Schema(description = "Last update timestamp of the lecture")
    private LocalDateTime updatedAt;
   
    private static Summary convertMapToSummary(Map<String, Object> summaryMap) {
        if (summaryMap == null) {
            return null;
        }
        
        try {
            // Metadata 변환
            Metadata metadata = convertToMetadata((Map<String, Object>) summaryMap.get("metadata"));
            
            // Overview 변환
            String overview = (String) summaryMap.get("overview");
            
            // Keywords 변환
            List<Keyword> keywords = convertToKeywords((List<Map<String, Object>>) summaryMap.get("keywords"));
            
            // Topics 변환
            List<TopicDetails> topics = convertToTopics((List<Map<String, Object>>) summaryMap.get("topics"));
            
            // Additional References 변환
            List<String> additionalReferences = convertToStringList((List<?>) summaryMap.get("additional_references"));
            
            return new Summary(metadata, overview, keywords, topics, additionalReferences);
            
        } catch (Exception e) {
            // 변환 실패 시 로그를 남기고 null 반환
            System.err.println("Summary 변환 중 오류 발생: " + e.getMessage());
            return null;
        }
    }

    /**
     * Map을 Metadata 객체로 변환
     */
    private static Metadata convertToMetadata(Map<String, Object> metadataMap) {
        if (metadataMap == null) {
            return null;
        }
        
        String model = (String) metadataMap.get("model");
        String createdAt = (String) metadataMap.get("created_at");
        
        return new Metadata(model, createdAt);
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
     * List<Map>을 List<TopicDetails>로 변환
     */
    private static List<TopicDetails> convertToTopics(List<Map<String, Object>> topicsList) {
        if (topicsList == null) {
            return new ArrayList<>();
        }
        
        List<TopicDetails> topics = new ArrayList<>();
        for (Map<String, Object> topicMap : topicsList) {
            if (topicMap != null) {
                TopicDetails topic = convertToTopicDetails(topicMap);
                if (topic != null) {
                    topics.add(topic);
                }
            }
        }
        return topics;
    }

    /**
     * Map을 TopicDetails 객체로 변환 (재귀적으로 sub_topics 처리)
     */
    private static TopicDetails convertToTopicDetails(Map<String, Object> topicMap) {
        if (topicMap == null) {
            return null;
        }
        
        String title = (String) topicMap.get("title");
        String description = (String) topicMap.get("description");
        
        PageRange pageRange = convertToPageRange((Map<String, Object>) topicMap.get("page_range"));
        
        List<String> additionalDetails = convertToStringList((List<?>) topicMap.get("additional_details"));
        
        // 재귀적으로 sub_topics 변환
        List<TopicDetails> subTopics = convertToTopics((List<Map<String, Object>>) topicMap.get("sub_topics"));
        
        return new TopicDetails(title, description, pageRange, additionalDetails, subTopics);
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

    /**
     * List<?>를 List<String>으로 변환
     */
    private static List<String> convertToStringList(List<?> list) {
        if (list == null) {
            return new ArrayList<>();
        }
        
        List<String> stringList = new ArrayList<>();
        for (Object item : list) {
            if (item != null) {
                stringList.add(item.toString());
            }
        }
        return stringList;
    }

    // 수정된 fromServiceDto 메서드
    public static LectureResponse fromServiceDto(LectureOutput lecture, StorageConfig storageConfig) {
        return new LectureResponse(
                lecture.getId(),
                lecture.getCourseId(),
                lecture.getUserId(),
                lecture.getTitle(),
                storageConfig.getFullMaterialUrl(lecture.getMaterialPath()),
                lecture.getDisplayOrderLex(),
                lecture.getParsedText(),
                lecture.getNote(),
                convertMapToSummary(lecture.getSummary()), // 여기서 변환 적용
                lecture.getSummaryStatus(),
                lecture.getCreatedAt(),
                lecture.getUpdatedAt()
        );
    }
}

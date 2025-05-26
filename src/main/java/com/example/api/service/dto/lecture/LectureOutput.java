package com.example.api.service.dto.lecture;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

import com.example.api.entity.Lecture;
import com.example.api.entity.enums.SummaryStatus;
import com.example.api.entity.ParsedText;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@NoArgsConstructor
@AllArgsConstructor
public class LectureOutput {
    private UUID id;
    private UUID courseId;
    private UUID userId;
    private String userName;
    private String userEmail;
    private String title;
    private String materialPath;
    private String materialType;
    private String displayOrderLex;
    private ParsedText parsedText;
    private Map<String, Object> note;
    private Map<String, Object> summary;
    private SummaryStatus summaryStatus;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static LectureOutput fromEntity(Lecture lecture) {
        return new LectureOutput(
                lecture.getId(),
                lecture.getCourse().getId(),
                lecture.getUser().getId(),
                lecture.getUser().getName(),
                lecture.getUser().getEmail(),
                lecture.getTitle(),
                lecture.getMaterialPath(),
                lecture.getMaterialType(),
                lecture.getDisplayOrderLex(),
                lecture.getParsedText(),
                lecture.getNote(),
                lecture.getSummary(),
                lecture.getSummaryStatus(),
                lecture.getCreatedAt(),
                lecture.getUpdatedAt()
        );
    }
}

package com.example.api.service.dto.exam;

import java.util.List;

import com.example.api.entity.ExamItem;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ExamItemListOutput {
    private List<ExamItemOutput> examItems;

    public static ExamItemListOutput fromEntities(List<ExamItem> examItems) {
        List<ExamItemOutput> examItemOutputs = examItems.stream()
                .map(ExamItemOutput::fromEntity)
                .toList();
        return new ExamItemListOutput(examItemOutputs);
    }
}

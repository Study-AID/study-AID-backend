package com.example.api.service.dto.lecture;

import java.util.UUID;

import com.example.api.entity.enums.SummaryStatus;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class UpdateLectureSummaryStatusInput {
    private UUID id;
    private SummaryStatus summaryStatus;
}

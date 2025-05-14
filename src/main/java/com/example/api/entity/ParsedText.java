package com.example.api.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ParsedText {
    @JsonProperty("total_pages")
    private int totalPages;

    private List<ParsedPage> pages;
}

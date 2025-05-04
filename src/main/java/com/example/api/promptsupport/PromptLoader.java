package com.example.api.promptsupport;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

import java.io.IOException;
import java.io.InputStream;

public class PromptLoader {

    private static final ObjectMapper YAML_MAPPER = new ObjectMapper(new YAMLFactory());

    private PromptLoader() {
        // static 유틸 클래스이므로 생성 방지
    }

    public static com.example.api.promptsupport.PromptTemplate load(String path) {
        try (InputStream inputStream = PromptLoader.class.getClassLoader().getResourceAsStream(path)) {
            if (inputStream == null) {
                throw new IllegalArgumentException("Prompt YAML file not found: " + path);
            }
            return YAML_MAPPER.readValue(inputStream, PromptTemplate.class);
        } catch (IOException e) {
            throw new RuntimeException("Failed to parse prompt YAML file: " + path, e);
        }
    }
}

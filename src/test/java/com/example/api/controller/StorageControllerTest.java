package com.example.api.controller;

import com.example.api.security.jwt.JwtAuthenticationFilter;
import com.example.api.security.jwt.JwtProvider;
import com.example.api.service.StorageService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityFilterAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(
    controllers = StorageController.class,
    excludeAutoConfiguration = {
            SecurityAutoConfiguration.class,
            SecurityFilterAutoConfiguration.class
    }
)
@ActiveProfiles("test")
class StorageControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private JwtProvider jwtProvider;
    
    @MockBean 
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @MockBean
    private StorageService storageService;

    @Test
    @DisplayName("파일 업로드 성공 시 200 OK 반환")
    void uploadSuccess() throws Exception {
        // Given
        MockMultipartFile file = new MockMultipartFile(
            "file", "test.pdf", MediaType.APPLICATION_PDF_VALUE, "dummy content".getBytes()
        );
        doNothing().when(storageService).upload(any());

        // When & Then
        mockMvc.perform(multipart("/api/storage/upload")
                    .file(file))
               .andExpect(status().isOk());
    }
}


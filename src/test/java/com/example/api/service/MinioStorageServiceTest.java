package com.example.api.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.web.servlet.MultipartProperties;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.errors.MinioException;
import io.minio.errors.XmlParserException;

@ExtendWith(SpringExtension.class)
@Import(MinioStorageService.class) 
@TestPropertySource(properties = {
    "minio.endpoint=http://minio:9000",
    "minio.access-key=test-access-key",
    "minio.secret-key=test-secret-key",
    "storage.bucket=test-bucket",
    "spring.servlet.multipart.max-file-size=50MB"
})
@DisplayName("MinioStorageService 테스트")
class MinioStorageServiceTest {
    
    @MockBean
    private MinioClient minioClient;
    
    @MockBean
    private MultipartProperties multipartProperties;
    
    @Autowired
    private MinioStorageService minioStorageService;
    
    @Test
    @DisplayName("PDF 파일 업로드 성공")
    void uploadPdfSuccess() throws IOException, NoSuchAlgorithmException, MinioException, XmlParserException, InvalidKeyException {
        // Given
        byte[] content = "PDF content".getBytes();
        MockMultipartFile pdfFile = new MockMultipartFile(
            "file", 
            "test.pdf", 
            "application/pdf", 
            content
        );
        
        // When
        when(multipartProperties.getMaxFileSize())
        .thenReturn(org.springframework.util.unit.DataSize.ofMegabytes(50));

        minioStorageService.upload(pdfFile);
        
        // Then
        ArgumentCaptor<PutObjectArgs> putObjectArgsCaptor = ArgumentCaptor.forClass(PutObjectArgs.class);
        
        verify(minioClient, times(1)).putObject(putObjectArgsCaptor.capture());
        
        PutObjectArgs capturedArgs = putObjectArgsCaptor.getValue();
        assertEquals("test-bucket", capturedArgs.bucket());
        assertEquals("application/pdf", capturedArgs.contentType());
    }
    
    @Test
    @DisplayName("50MB 초과 파일 업로드 실패")
    void uploadLargeFileFail() {
        // Given
        MockMultipartFile largePdfFile = mock(MockMultipartFile.class);
        when(largePdfFile.getSize()).thenReturn(51L * 1024 * 1024); // 51MB
        when(multipartProperties.getMaxFileSize())
        .thenReturn(org.springframework.util.unit.DataSize.ofMegabytes(50));
        
        // When & Then
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class, 
            () -> minioStorageService.upload(largePdfFile)
        );

        assertEquals("50 MB 초과", exception.getMessage());
    }
}
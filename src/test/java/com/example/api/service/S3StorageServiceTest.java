
package com.example.api.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.web.servlet.MultipartProperties;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.web.multipart.MultipartFile;

import com.example.api.config.StorageProperties;

import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;


@ExtendWith(SpringExtension.class)
@Import({S3StorageServiceTest.TestConfig.class})
@TestPropertySource(properties = {
    "storage.bucket=test-bucket",
    "spring.servlet.multipart.max-file-size=50MB"
})
@DisplayName("S3StorageService 테스트")
class S3StorageServiceTest {
    
    @TestConfiguration
    static class TestConfig {
        @Bean
        public StorageProperties storageProperties() {
            StorageProperties properties = new StorageProperties();
            properties.setBucket("test-bucket");
            return properties;
        }
        
        @Bean
        public S3StorageService s3StorageService(S3Client s3Client,
                                                MultipartProperties multipartProperties,
                                                StorageProperties storageProperties) {
            return new S3StorageService(s3Client, multipartProperties, storageProperties);
        }
    }
    
    @MockBean
    private S3Client s3Client;
    
    @MockBean
    private MultipartProperties multipartProperties;
    
    @Autowired
    private S3StorageService s3storageService;
    
    @Test
    @DisplayName("PDF 파일 업로드 성공")
    void uploadPdfSuccess() throws Exception {
        // Given
        byte[] content = "PDF content".getBytes();
        
        // Mock InputStream to avoid null pointer exception
        InputStream inputStream = new ByteArrayInputStream(content);
        MultipartFile pdfFile = mock(MultipartFile.class);
        when(pdfFile.getInputStream()).thenReturn(inputStream);
        when(pdfFile.getSize()).thenReturn((long) content.length);
        
        when(s3Client.putObject(any(PutObjectRequest.class), any(RequestBody.class))).thenReturn(null);
        when(multipartProperties.getMaxFileSize())
        .thenReturn(org.springframework.util.unit.DataSize.ofMegabytes(50));
        
        // When
        s3storageService.upload(pdfFile);
        
        // Then
        ArgumentCaptor<PutObjectRequest> putObjectRequestCaptor = ArgumentCaptor.forClass(PutObjectRequest.class);
        ArgumentCaptor<RequestBody> requestBodyCaptor = ArgumentCaptor.forClass(RequestBody.class);
        
        verify(s3Client, times(1)).putObject(putObjectRequestCaptor.capture(), requestBodyCaptor.capture());
        
        PutObjectRequest capturedRequest = putObjectRequestCaptor.getValue();
        assertEquals("test-bucket", capturedRequest.bucket());
        assertEquals("application/pdf", capturedRequest.contentType());
        assertEquals(content.length, capturedRequest.contentLength());
    }
    
    @Test
    @DisplayName("용량 초과 파일 업로드 실패")
    void uploadLargeFileFail() {
        // Given
        MockMultipartFile largePdfFile = mock(MockMultipartFile.class);
        when(largePdfFile.getSize()).thenReturn(51L * 1024 * 1024); // 51MB
        when(multipartProperties.getMaxFileSize())
        .thenReturn(org.springframework.util.unit.DataSize.ofMegabytes(50));
        
        // When & Then
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class, 
            () -> s3storageService.upload(largePdfFile)
        );
        
        String expectedMessage = String.format("파일 크기는 %dMB를 초과할 수 없습니다.", multipartProperties.getMaxFileSize().toBytes() / 1024 / 1024);
        assertEquals(expectedMessage, exception.getMessage());
    }
}
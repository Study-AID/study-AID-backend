package com.example.api.service;

import com.example.api.config.StorageConfig;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.web.servlet.MultipartProperties;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(SpringExtension.class)
@Import({MinioStorageServiceTest.TestConfig.class})
@TestPropertySource(properties = {
        "storage.bucket=test-bucket",
        "spring.servlet.multipart.max-file-size=50MB"
})
@DisplayName("MinioStorageService 테스트")
class MinioStorageServiceTest {

    @TestConfiguration
    static class TestConfig {
        @Bean
        public StorageConfig storageConfig() {
            StorageConfig config = new StorageConfig();
            config.setBucket("test-bucket");
            return config;
        }

        @Bean
        public MinioStorageService minioStorageService(MinioClient minioClient,
                                                       MultipartProperties multipartProperties,
                                                       StorageConfig storageConfig) {
            return new MinioStorageService(minioClient, multipartProperties, storageConfig);
        }
    }

    @MockBean
    private MinioClient minioClient;

    @MockBean
    private MultipartProperties multipartProperties;

    @Autowired
    private MinioStorageService minioStorageService;

    @Test
    @DisplayName("PDF 파일 업로드 성공")
    void uploadPdfSuccess() throws Exception {
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

        String result = minioStorageService.upload(pdfFile);

        // Then
        ArgumentCaptor<PutObjectArgs> putObjectArgsCaptor = ArgumentCaptor.forClass(PutObjectArgs.class);

        verify(minioClient, times(1)).putObject(putObjectArgsCaptor.capture());

        PutObjectArgs capturedArgs = putObjectArgsCaptor.getValue();
        assertEquals("test-bucket", capturedArgs.bucket());
        assertEquals("application/pdf", capturedArgs.contentType());
        assertEquals(result.endsWith(".pdf"), true);
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

        String expectedMessage = String.format("파일 크기는 %dMB를 초과할 수 없습니다.", multipartProperties.getMaxFileSize().toBytes() / 1024 / 1024);
        assertEquals(expectedMessage, exception.getMessage());
    }

    @Test
    @DisplayName("PDF가 아닌 파일 업로드 실패")
    void uploadNonPdfFileFail() {
        // Given
        byte[] content = "Text content".getBytes();
        MockMultipartFile textFile = new MockMultipartFile(
                "file",
                "test.txt",
                "text/plain",
                content
        );
        when(multipartProperties.getMaxFileSize())
                .thenReturn(org.springframework.util.unit.DataSize.ofMegabytes(50));

        // When & Then
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> minioStorageService.upload(textFile)
        );

        assertEquals("Only PDF files are supported", exception.getMessage());
    }
}
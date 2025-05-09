package com.example.api.service;

import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.autoconfigure.web.servlet.MultipartProperties;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;

import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StorageServiceTest {

    @Mock
    private S3Client mockS3Client;

    @Mock
    private MinioClient mockMinioClient;

    private S3StorageService s3StorageService;
    private MinioStorageService minioStorageService;

    @Mock
    private MultipartProperties multipartProperties;

    @BeforeEach
    void setUp() {
        // S3 전용 서비스
        s3StorageService = new S3StorageService(
            Optional.of(mockS3Client),
            multipartProperties
        );
        ReflectionTestUtils.setField(s3StorageService, "bucket", "test-bucket");

        // MinIO 전용 서비스
        minioStorageService = new MinioStorageService(
            Optional.of(mockMinioClient),
            multipartProperties
        );
        ReflectionTestUtils.setField(minioStorageService, "bucket", "test-bucket");
    }

    @Test
    @DisplayName("S3Client.putObject 호출 검증")
    void upload_callsS3ClientPutObject() throws Exception {
        // given
        byte[] data = "hello".getBytes();
        MockMultipartFile file = new MockMultipartFile(
            "file", "test.pdf",
            "application/pdf", data
        );

        // when
        when(multipartProperties.getMaxFileSize())
        .thenReturn(org.springframework.util.unit.DataSize.ofMegabytes(50));

        s3StorageService.upload(file);

        // then
        ArgumentCaptor<PutObjectRequest> reqCaptor =
            ArgumentCaptor.forClass(PutObjectRequest.class);
        ArgumentCaptor<RequestBody> bodyCaptor =
            ArgumentCaptor.forClass(RequestBody.class);

        verify(mockS3Client, times(1))
            .putObject(reqCaptor.capture(), bodyCaptor.capture());

        PutObjectRequest req = reqCaptor.getValue();
        assertTrue("test-bucket".equals(req.bucket()));
        assertTrue(req.contentLength() == data.length);
        assertTrue("application/pdf".equals(req.contentType()));
    }

    @Test
    @DisplayName("MinioClient.putObject 호출 검증")
    void upload_callsMinioClientPutObject() throws Exception {
        // given
        byte[] data = "hello".getBytes();
        MockMultipartFile file = new MockMultipartFile(
            "file", "test.pdf",
            "application/pdf", data
        );

        // when
        when(multipartProperties.getMaxFileSize())
        .thenReturn(org.springframework.util.unit.DataSize.ofMegabytes(50));

        minioStorageService.upload(file);

        // then
        ArgumentCaptor<PutObjectArgs> argsCaptor =
            ArgumentCaptor.forClass(PutObjectArgs.class);

        verify(mockMinioClient, times(1))
            .putObject(argsCaptor.capture());

        PutObjectArgs args = argsCaptor.getValue();
        assertTrue("test-bucket".equals(args.bucket()));
        assertTrue(args.object().endsWith(".pdf"));
        assertTrue("application/pdf".equals(args.contentType()));
    }

    @Test
    @DisplayName("50MB 초과 업로드 시 IllegalArgumentException 발생")
    void upload_sizeExceeds_throwsException() {
        // given: 크기 자체만 51MB로 지정
        MockMultipartFile bigFile = new MockMultipartFile(
            "file", "big.pdf",
            "application/pdf", new byte[]{}
        ) {
            @Override
            public long getSize() {
                return 51L * 1024 * 1024;
            }
        };
        // when
        when(multipartProperties.getMaxFileSize())
        .thenReturn(org.springframework.util.unit.DataSize.ofMegabytes(50));

        // then: S3 모드
        assertThrows(IllegalArgumentException.class, () -> {
            s3StorageService.upload(bigFile);
        });

        // then: MinIO 모드
        assertThrows(IllegalArgumentException.class, () -> {
            minioStorageService.upload(bigFile);
        });
    }
}

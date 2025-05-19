package com.example.api.service;

import com.example.api.config.StorageConfig;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import org.springframework.boot.autoconfigure.web.servlet.MultipartProperties;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.UUID;

@Service
@Profile("local")
public class MinioStorageService implements StorageService {
    private final MinioClient minioClient;
    private final MultipartProperties multipartProperties;
    private final StorageConfig storageConfig;

    public MinioStorageService(MinioClient minioClient,
                               MultipartProperties multipartProperties,
                               StorageConfig storageConfig) {
        this.minioClient = minioClient;
        this.multipartProperties = multipartProperties;
        this.storageConfig = storageConfig;
    }

    @Override
    public String upload(MultipartFile pdf) throws Exception {
        long size = pdf.getSize();
        long maxFileSize = multipartProperties.getMaxFileSize().toBytes();

        if (size > maxFileSize) {
            throw new IllegalArgumentException(
                    String.format("파일 크기는 %dMB를 초과할 수 없습니다.", maxFileSize / 1024 / 1024)
            );
        }

        // Validate PDF file
        String contentType = pdf.getContentType();
        String filename = pdf.getOriginalFilename();

        if (!"application/pdf".equals(contentType) &&
                (filename == null || !filename.toLowerCase().endsWith(".pdf"))) {
            throw new IllegalArgumentException("Only PDF files are supported");
        }

        String key = UUID.randomUUID() + ".pdf";
        try (InputStream in = pdf.getInputStream()) {
            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(storageConfig.getBucket())
                            .object(key)
                            .stream(in, size, -1)
                            .contentType("application/pdf")
                            .build());
            return key;
        }
    }

    @Override
    public String getBucket() {
        return storageConfig.getBucket();
    }
}

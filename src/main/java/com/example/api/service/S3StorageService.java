package com.example.api.service;

import com.example.api.config.StorageConfig;
import org.springframework.boot.autoconfigure.web.servlet.MultipartProperties;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.InputStream;
import java.util.UUID;

@Service
@Profile({"dev", "prod"})
public class S3StorageService implements StorageService {
    private final S3Client s3Client;
    private final MultipartProperties multipartProperties;
    private final StorageConfig storageConfig;

    public S3StorageService(S3Client s3Client,
                            MultipartProperties multipartProperties,
                            StorageConfig storageConfig) {
        this.s3Client = s3Client;
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
            s3Client.putObject(
                    PutObjectRequest.builder()
                            .bucket(storageConfig.getBucket())
                            .key(key)
                            .contentType("application/pdf")
                            .contentLength(size)
                            .build(),
                    RequestBody.fromInputStream(in, size));
            return key;
        }
    }

    @Override
    public String getBucket() {
        return storageConfig.getBucket();
    }
}

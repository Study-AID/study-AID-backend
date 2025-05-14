package com.example.api.service;

import java.io.InputStream;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.web.servlet.MultipartProperties;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.example.api.config.StorageProperties;

import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

@Service
@Profile("prod")
public class S3StorageService implements StorageService {
    private final S3Client s3Client;
    private final MultipartProperties multipartProperties;
    private final StorageProperties storageProperties;

    public S3StorageService(S3Client s3Client,
                           MultipartProperties multipartProperties,
                           StorageProperties storageProperties) {
        this.s3Client = s3Client;
        this.multipartProperties = multipartProperties;
        this.storageProperties = storageProperties;
    }

    @Override
    public void upload(MultipartFile pdf) throws Exception {
        long size = pdf.getSize();
        long maxFileSize = multipartProperties.getMaxFileSize().toBytes();

        if (size > maxFileSize) {
            throw new IllegalArgumentException(
                String.format("파일 크기는 %dMB를 초과할 수 없습니다.", maxFileSize / 1024 / 1024)
            );
        }

        String key = UUID.randomUUID() + ".pdf";
        try (InputStream in = pdf.getInputStream()) {
            s3Client.putObject(
                PutObjectRequest.builder()
                    .bucket(storageProperties.getBucket())
                    .key(key)
                    .contentType("application/pdf")
                    .contentLength(size)
                    .build(),
                RequestBody.fromInputStream(in, size));
        }
    }
}

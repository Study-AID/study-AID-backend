package com.example.api.service;

import java.io.InputStream;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.web.servlet.MultipartProperties;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import lombok.RequiredArgsConstructor;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

@Service
@RequiredArgsConstructor
public class S3StorageService implements StorageService {
    private final Optional<S3Client> s3Client;
    private final MultipartProperties multipartProperties;

    @Value("${storage.bucket}")
    private String bucket;

    @Override
    public void upload(MultipartFile pdf) throws Exception {
        long size = pdf.getSize();
        long maxFileSize = multipartProperties.getMaxFileSize().toBytes();

        if (size > maxFileSize)
            throw new IllegalArgumentException(String.format("파일 크기는 %dMB를 초과할 수 없습니다.", maxFileSize / 1024 / 1024));

        String key = UUID.randomUUID() + ".pdf";
        try (InputStream in = pdf.getInputStream()) {
            s3Client.get().putObject(
                PutObjectRequest.builder()
                    .bucket(bucket)
                    .key(key)
                    .contentType("application/pdf")
                    .contentLength(size)
                    .build(),
                RequestBody.fromInputStream(in, size));
        }
    }
}

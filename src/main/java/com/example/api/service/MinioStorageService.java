package com.example.api.service;

import java.io.InputStream;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.web.servlet.MultipartProperties;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.example.api.config.StorageProperties;

import io.minio.MinioClient;
import io.minio.PutObjectArgs;

@Service
@Profile("local")
public class MinioStorageService implements StorageService {
    private final MinioClient minioClient;
    private final MultipartProperties multipartProperties;
    private final StorageProperties storageProperties;

    public MinioStorageService(MinioClient minioClient,
                              MultipartProperties multipartProperties,
                              StorageProperties storageProperties) {
        this.minioClient = minioClient;
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
            minioClient.putObject(
                PutObjectArgs.builder()
                    .bucket(storageProperties.getBucket())
                    .object(key)
                    .stream(in, size, -1)
                    .contentType("application/pdf")
                    .build());
        }
    }
}

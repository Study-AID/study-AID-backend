package com.example.api.service;

import java.io.IOException;
import java.io.InputStream;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.web.servlet.MultipartProperties;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.errors.MinioException;
import io.minio.errors.XmlParserException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MinioStorageService implements StorageService {
    private final Optional<MinioClient> minioClient;
    private final MultipartProperties multipartProperties;

    @Value("${storage.bucket}")
    private String bucket;

    @Override
    public void upload(MultipartFile pdf) throws IOException, NoSuchAlgorithmException, XmlParserException, MinioException, InvalidKeyException, IllegalArgumentException {
        long size = pdf.getSize();
        long maxFileSize = multipartProperties.getMaxFileSize().toBytes();

        if (size > maxFileSize)
            throw new IllegalArgumentException(String.format("파일 크기는 %dMB를 초과할 수 없습니다.", maxFileSize / 1024 / 1024));

        String key = UUID.randomUUID() + ".pdf";
        try (InputStream in = pdf.getInputStream()) {
            minioClient.get().putObject(
                PutObjectArgs.builder()
                    .bucket(bucket)
                    .object(key)
                    .stream(in, size, -1)
                    .contentType("application/pdf")
                    .build());
        }
    }
}

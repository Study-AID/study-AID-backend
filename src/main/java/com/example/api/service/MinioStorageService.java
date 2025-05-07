package com.example.api.service;

import java.io.IOException;
import java.io.InputStream;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.errors.MinioException;
import io.minio.errors.XmlParserException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MinioStorageService {
    private final Optional<MinioClient> minioClient;

    @Value("${file.bucket}")
    private String bucket;

    public void upload(MultipartFile pdf) throws IOException, NoSuchAlgorithmException, XmlParserException, MinioException, InvalidKeyException, IllegalArgumentException {
        long size = pdf.getSize();
        if (size > 50 * 1024 * 1024)
            throw new IllegalArgumentException("50 MB 초과");

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

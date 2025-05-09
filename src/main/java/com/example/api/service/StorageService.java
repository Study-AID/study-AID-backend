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
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

@Service
@RequiredArgsConstructor
public class StorageService {

    private final Optional<S3Client> s3Client;
    private final Optional<MinioClient> minioClient;

    @Value("${file.bucket}")
    private String bucket;

    public void upload(MultipartFile pdf) throws IOException, NoSuchAlgorithmException, XmlParserException, MinioException, InvalidKeyException, IllegalArgumentException {
        long size = pdf.getSize();
        if (size > 50 * 1024 * 1024)
            throw new IllegalArgumentException("50 MB 초과");

        String key = UUID.randomUUID() + ".pdf";
        try (InputStream in = pdf.getInputStream()) {
            if (s3Client.isPresent()) {
                s3Client.get().putObject(
                    PutObjectRequest.builder()
                        .bucket(bucket)
                        .key(key)
                        .contentType("application/pdf")
                        .contentLength(size)
                        .build(),
                    RequestBody.fromInputStream(in, size));
            } else {
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
}

package com.example.api.service;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import org.springframework.web.multipart.MultipartFile;

import io.minio.errors.MinioException;
import io.minio.errors.XmlParserException;

public interface StorageService {
    public void upload(MultipartFile pdf) throws IOException, NoSuchAlgorithmException, XmlParserException, MinioException, InvalidKeyException, IllegalArgumentException;
}

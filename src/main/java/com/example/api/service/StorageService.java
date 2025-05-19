package com.example.api.service;

import org.springframework.web.multipart.MultipartFile;

public interface StorageService {
    public String upload(MultipartFile pdf) throws Exception;

    public String getBucket();
}

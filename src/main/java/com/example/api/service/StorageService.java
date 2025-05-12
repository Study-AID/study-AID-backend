package com.example.api.service;

import org.springframework.web.multipart.MultipartFile;

public interface StorageService {
    public void upload(MultipartFile pdf) throws Exception;
}

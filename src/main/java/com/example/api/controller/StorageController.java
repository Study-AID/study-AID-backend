package com.example.api.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import com.example.api.service.StorageService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/v1/storage")
@Tag(name = "Storage", description = "Storage API")
@RequiredArgsConstructor
public class StorageController {
    private final StorageService storageService;

    @PostMapping("/upload")
    public ResponseEntity<Void> upload(@RequestParam MultipartFile file) {
        try {
            storageService.upload(file);
            return ResponseEntity.ok().build();
        } catch (IllegalArgumentException e) {
            // 파일 크기 초과 등 클라이언트 예외
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage(), e);
        } catch (Exception e) {
            throw new ResponseStatusException(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "파일 업로드 처리 중 오류가 발생했습니다.",
                e
            );
        }
    }
}


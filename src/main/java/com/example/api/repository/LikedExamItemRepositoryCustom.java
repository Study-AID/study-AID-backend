package com.example.api.repository;

import java.util.Optional;
import java.util.UUID;

import com.example.api.entity.LikedExamItem;

public interface LikedExamItemRepositoryCustom {
    Optional<LikedExamItem> findByExamItemIdAndUserId(UUID examItemId, UUID userId);
    
    LikedExamItem createLikedExamItem(LikedExamItem likedExamItem);
    
    void deleteLikedExamItem(UUID likedExamItemId);
}

package com.example.api.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.api.entity.LikedExamItem;

public interface LikedExamItemRepository extends JpaRepository<LikedExamItem, UUID>, LikedExamItemRepositoryCustom {
    Optional<LikedExamItem> findByExamItemIdAndUserId(UUID examItemId, UUID userId);
    
    LikedExamItem createLikedExamItem(LikedExamItem likedExamItem);
    
    void deleteLikedExamItem(UUID likedExamItemId);
}
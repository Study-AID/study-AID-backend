package com.example.api.repository;

import com.example.api.entity.LikedQuizItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface LikedQuizItemRepository extends JpaRepository<LikedQuizItem, UUID> {
    Optional<LikedQuizItem> findByQuizItemId(UUID quizItemId);
    
    Optional<LikedQuizItem> findByQuizItemIdAndUserId(UUID quizItemId, UUID userId);

    LikedQuizItem createLikedQuizItem(LikedQuizItem likedQuizItem);

    void deleteLikedQuizItem(UUID likedQuizItemId);
}

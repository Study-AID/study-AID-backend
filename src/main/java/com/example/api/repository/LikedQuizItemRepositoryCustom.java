package com.example.api.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.example.api.entity.LikedQuizItem;

public interface LikedQuizItemRepositoryCustom {
    Optional<LikedQuizItem> findByQuizItemId(UUID quizItemId);

    List<LikedQuizItem> findByLectureId(UUID lectureId);
    
    LikedQuizItem createLikedQuizItem(LikedQuizItem likedQuizItem);

    void deleteLikedQuizItem(UUID likedQuizItemId);
}

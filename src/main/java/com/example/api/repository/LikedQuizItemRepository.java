package com.example.api.repository;

import com.example.api.entity.LikedQuizItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface LikedQuizItemRepository extends JpaRepository<LikedQuizItem, UUID>, LikedQuizItemRepositoryCustom {    
    Optional<LikedQuizItem> findByQuizItemId(UUID quizItemId);

    List<LikedQuizItem> findByLectureId(UUID lectureId);

    LikedQuizItem createLikedQuizItem(LikedQuizItem likedQuizItem);

    void deleteLikedQuizItem(UUID likedQuizItemId);
}

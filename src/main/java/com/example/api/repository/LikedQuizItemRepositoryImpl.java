package com.example.api.repository;

import java.util.Optional;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.example.api.entity.LikedQuizItem;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

@Repository
public class LikedQuizItemRepositoryImpl implements LikedQuizItemRepositoryCustom {
    private static final Logger logger = LoggerFactory.getLogger(LikedQuizItemRepositoryImpl.class);

    @PersistenceContext
    private EntityManager manager;

    @Override
    public Optional<LikedQuizItem> findByQuizItemId(UUID quizItemId) {
        try {
            LikedQuizItem likedQuizItem = manager.createQuery(
                    "SELECT l FROM LikedQuizItem l " +
                            "WHERE l.quizItem.id = :quizItemId ",
                    LikedQuizItem.class)
                    .setParameter("quizItemId", quizItemId)
                    .getSingleResult();
            return Optional.of(likedQuizItem);
        } catch (Exception e) {
            logger.error("Error finding liked quiz item by quiz item ID: {}", quizItemId, e);
            return Optional.empty();
        }
    }

    @Override
    public Optional<LikedQuizItem> findByQuizItemIdAndUserId(UUID quizItemId, UUID userId) {
        try {
            LikedQuizItem likedQuizItem = manager.createQuery(
                    "SELECT l FROM LikedQuizItem l " +
                            "WHERE l.quizItem.id = :quizItemId " +
                            "AND l.user.id = :userId",
                    LikedQuizItem.class)
                    .setParameter("quizItemId", quizItemId)
                    .setParameter("userId", userId)
                    .getSingleResult();
            return Optional.of(likedQuizItem);
        } catch (Exception e) {
            logger.debug("No liked quiz item found for quizItemId: {} and userId: {}", quizItemId, userId);
            return Optional.empty();
        }
    }

    @Override
    @Transactional
    public LikedQuizItem createLikedQuizItem(LikedQuizItem likedQuizItem) {
        if (isDuplicated(likedQuizItem.getQuizItem().getId(), likedQuizItem.getUser().getId())) {
            throw new IllegalArgumentException(
                    "Liked quiz item with the same quiz item and user already exists");
        }
        manager.persist(likedQuizItem);
        return likedQuizItem;
    }

    private boolean isDuplicated(UUID quizItemId, UUID userId) {
        return manager.createQuery(
                "SELECT COUNT(l) > 0 FROM LikedQuizItem l " +
                        "WHERE l.quizItem.id = :quizItemId " +
                        "AND l.user.id = :userId ",
                Boolean.class)
                .setParameter("quizItemId", quizItemId)
                .setParameter("userId", userId)
                .getSingleResult();
    }

    @Override
    public void deleteLikedQuizItem(UUID likedQuizItemId) {
        // hard delete
        try {
            LikedQuizItem likedQuizItem = manager.find(LikedQuizItem.class, likedQuizItemId);
            if (likedQuizItem != null) {
                manager.remove(likedQuizItem);
            } else {
                logger.warn("LikedQuizItem with ID {} not found for deletion", likedQuizItemId);
            }
        } catch (Exception e) {
            logger.error("Error deleting liked quiz item with ID: {}", likedQuizItemId, e);
            throw new RuntimeException("Failed to delete liked quiz item", e);
        }
    }
}

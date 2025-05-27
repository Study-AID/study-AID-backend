package com.example.api.repository;

import java.util.Optional;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.example.api.entity.LikedExamItem;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

@Repository
public class LikedExamItemRepositoryImpl implements LikedExamItemRepositoryCustom {
    private static final Logger logger = LoggerFactory.getLogger(LikedExamItemRepositoryImpl.class);

    @PersistenceContext
    private EntityManager manager;

    @Override
    public Optional<LikedExamItem> findByExamItemIdAndUserId(UUID examItemId, UUID userId) {
        try {
            LikedExamItem likedExamItem = manager.createQuery(
                    "SELECT l FROM LikedExamItem l " +
                            "WHERE l.examItem.id = :examItemId " +
                            "AND l.user.id = :userId",
                    LikedExamItem.class)
                    .setParameter("examItemId", examItemId)
                    .setParameter("userId", userId)
                    .getSingleResult();
            return Optional.of(likedExamItem);
        } catch (Exception e) {
            logger.debug("No liked exam item found for examItemId: {} and userId: {}", examItemId, userId);
            return Optional.empty();
        }
    }

    @Override
    @Transactional
    public LikedExamItem createLikedExamItem(LikedExamItem likedExamItem) {
        if (isDuplicated(likedExamItem.getExamItem().getId(), likedExamItem.getUser().getId())) {
            throw new IllegalArgumentException(
                    "Liked exam item with the same exam item and user already exists");
        }
        manager.persist(likedExamItem);
        return likedExamItem;
    }

    private boolean isDuplicated(UUID examItemId, UUID userId) {
        return manager.createQuery(
                "SELECT COUNT(l) > 0 FROM LikedExamItem l " +
                        "WHERE l.examItem.id = :examItemId " +
                        "AND l.user.id = :userId ",
                Boolean.class)
                .setParameter("examItemId", examItemId)
                .setParameter("userId", userId)
                .getSingleResult();
    }

    @Override
    @Transactional
    public void deleteLikedExamItem(UUID likedExamItemId) {
        try {
            LikedExamItem likedExamItem = manager.find(LikedExamItem.class, likedExamItemId);
            if (likedExamItem != null) {
                manager.remove(likedExamItem);
            } else {
                logger.debug("No liked exam item found with id: {}", likedExamItemId);
            }
        } catch (Exception e) {
            logger.error("Error deleting liked exam item with id: {}", likedExamItemId, e);
            throw new RuntimeException("Error deleting liked exam item", e);
        }
    }
}

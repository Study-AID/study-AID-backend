package com.example.api.repository;

import com.example.api.entity.Semester;
import com.example.api.entity.enums.Season;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.PersistenceContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class SemesterRepositoryImpl implements SemesterRepositoryCustom {
    private static final Logger logger = LoggerFactory.getLogger(SemesterRepositoryImpl.class);

    @PersistenceContext
    private EntityManager manager;

    @Override
    public List<Semester> findByUserId(UUID userId) {
        return manager.createQuery(
                        "SELECT s FROM Semester s " +
                                "WHERE s.user.id = :userId" +
                                " AND s.deletedAt IS NULL",
                        Semester.class)
                .setParameter("userId", userId)
                .getResultList();
    }

    @Override
    public Optional<Semester> findByUserIdAndYearAndSeason(UUID userId, int year, Season season) {
        try {
            Semester semester = manager.createQuery(
                            "SELECT s FROM Semester s " +
                                    "WHERE s.user.id = :userId " +
                                    "AND s.year = :year " +
                                    "AND s.season = :season " +
                                    "AND s.deletedAt IS NULL",
                            Semester.class)
                    .setParameter("userId", userId)
                    .setParameter("year", year)
                    .setParameter("season", season)
                    .getSingleResult();
            return Optional.of(semester);
        } catch (NoResultException e) {
            return Optional.empty();
        }
    }

    @Override
    @Transactional
    public Semester createSemester(Semester semester) {
        if (isDuplicated(semester.getUser().getId(), semester.getYear(), semester.getSeason())) {
            throw new IllegalArgumentException(
                    "Semester with the same year and season already exists"
            );
        }
        manager.persist(semester);
        return semester;
    }

    @Override
    @Transactional
    public Semester updateSemester(Semester semester) {
        return manager.merge(semester);
    }

    private boolean isDuplicated(UUID userId, int year, Season season) {
        Long count = manager.createQuery(
                        "SELECT COUNT(s) " +
                                "FROM Semester s " +
                                "WHERE s.user.id = :userId " +
                                "AND s.year = :year " +
                                "AND s.season = :season " +
                                "AND s.deletedAt IS NULL",
                        Long.class)
                .setParameter("userId", userId)
                .setParameter("year", year)
                .setParameter("season", season)
                .getSingleResult();
        return count > 0;
    }

    @Override
    @Transactional
    public void deleteSemester(UUID semesterId) {
        try {
            Semester semester = manager.find(Semester.class, semesterId);
            if (semester != null) {
                semester.setDeletedAt(LocalDateTime.now());
                manager.merge(semester);
                logger.info("Soft deleted semester with id: {}", semesterId);
            } else {
                logger.warn("Semester not found with id: {}", semesterId);
            }
        } catch (Exception e) {
            logger.error("Error deleting semester with id: {}", semesterId, e);
            throw new RuntimeException("Failed to delete semester", e);
        }
    }
}
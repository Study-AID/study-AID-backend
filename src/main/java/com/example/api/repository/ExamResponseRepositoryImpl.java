package com.example.api.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import com.example.api.entity.ExamResponse;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;

@Repository
public class ExamResponseRepositoryImpl implements ExamResponseRepositoryCustom {
    private static final Logger logger = LoggerFactory.getLogger(ExamResponseRepositoryImpl.class);

    @PersistenceContext
    private EntityManager manager;

    public List<ExamResponse> findByExamId(UUID examId) {
        return manager.createQuery(
                        "SELECT er FROM ExamResponse er " +
                                "WHERE er.exam.id = :examId " +
                                "AND er.deletedAt IS NULL " +
                                "ORDER BY er.createdAt DESC",
                        ExamResponse.class)
                .setParameter("examId", examId)
                .getResultList();
    }

    
    public Optional<ExamResponse> findByExamItemId(UUID examItemId) {
        return manager.createQuery(
                        "SELECT er FROM ExamResponse er " +
                                "WHERE er.examItem.id = :examItemId " +
                                "AND er.deletedAt IS NULL",
                        ExamResponse.class)
                .setParameter("examItemId", examItemId)
                .getResultStream()
                .findFirst();
    }
    
    @Transactional
    public ExamResponse createExamResponse(ExamResponse examResponse) {
        if (isDuplicated(examResponse.getExam().getId(), examResponse.getExamItem().getId())) {
            throw new IllegalArgumentException(
                    "Exam response with the same exam and exam item already exists"
            );
        }
        manager.persist(examResponse);
        return examResponse;
    }

    private boolean isDuplicated(UUID examId, UUID examItemId) {
        return manager.createQuery(
                        "SELECT COUNT(er) > 0 FROM ExamResponse er " +
                                "WHERE er.exam.id = :examId " +
                                "AND er.examItem.id = :examItemId " +
                                "AND er.deletedAt IS NULL",
                        Boolean.class)
                .setParameter("examId", examId)
                .setParameter("examItemId", examItemId)
                .getSingleResult();
    }

    @Transactional
    public ExamResponse updateExamResponse(ExamResponse examResponse) {
        return manager.merge(examResponse);
    }

    @Transactional
    public void deleteExamResponse(UUID examResponseId) {
        ExamResponse examResponse = manager.find(ExamResponse.class, examResponseId);
        try{
            if (examResponse != null) {
                manager.merge(examResponse);
                logger.info("Soft delete exam response with ID: {}", examResponseId); 
            } else {
                logger.warn("Exam response with ID: {} not found for deletion", examResponseId);
            }
        } catch (Exception e) {
            logger.error("Error deleting exam response with ID: {}", examResponseId, e.getMessage());
            throw new RuntimeException("Error deleting exam response", e);
        }
    }
}

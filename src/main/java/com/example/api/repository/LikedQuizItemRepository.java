package com.example.api.repository;

import com.example.api.entity.LikedQuizItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface LikedQuizItemRepository extends JpaRepository<LikedQuizItem, UUID> {

}

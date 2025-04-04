package com.example.api.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.api.entity.LikedQuizItem;

public interface LikedQuizItemRepository extends JpaRepository<LikedQuizItem, UUID> {

}

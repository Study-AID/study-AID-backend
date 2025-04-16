package com.example.api.repository;

import com.example.api.entity.LikedQnaAnswer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface LikedQnaAnswerRepository extends JpaRepository<LikedQnaAnswer, UUID> {

}

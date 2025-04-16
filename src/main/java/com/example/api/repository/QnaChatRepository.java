package com.example.api.repository;

import com.example.api.entity.QnaChat;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface QnaChatRepository extends JpaRepository<QnaChat, UUID> {

}

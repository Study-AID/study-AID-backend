package com.example.api.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.api.entity.QnaChatMessage;

public interface QnaChatMessageRepository extends JpaRepository<QnaChatMessage, UUID> {

}

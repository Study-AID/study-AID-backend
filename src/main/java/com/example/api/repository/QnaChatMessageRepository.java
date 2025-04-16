package com.example.api.repository;

import com.example.api.entity.QnaChatMessage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface QnaChatMessageRepository extends JpaRepository<QnaChatMessage, UUID> {

}

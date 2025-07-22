package com.example.smartstudy.repository;

import com.example.smartstudy.model.ChatMessage;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {}
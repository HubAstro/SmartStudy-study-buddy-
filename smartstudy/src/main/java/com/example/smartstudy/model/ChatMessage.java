package com.example.smartstudy.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "chat_messages")
public class ChatMessage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Lob
    @Column(columnDefinition = "TEXT")
    private String content;

    private String sender; // "USER" or "GEMINI"
    private LocalDateTime createdAt;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "file_id")
    private StudyFile studyFile;
    
    public Long getId() {
    	return id;
    }
    
    public void setId(Long id) {
    	this.id = id;
    }
    
    public String getContent() {
    	return content;
    }
    
    public void setContent(String content) {
    	this.content = content;
    }
    
    public String getSender() {
    	return sender;
    }
    
    public void setSender(String sender) {
    	this.sender = sender;
    }
    
    public LocalDateTime getCreatedAt() {
    	return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
    	this.createdAt = createdAt;
    }
    
    public StudyFile getStudyFile() {
    	return studyFile;
    }
    
    public void setStudyFile(StudyFile studyFile) {
    	this.studyFile = studyFile;
    }
    
    
}
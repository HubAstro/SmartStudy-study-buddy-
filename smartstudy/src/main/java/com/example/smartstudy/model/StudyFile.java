package com.example.smartstudy.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "study_files")
public class StudyFile {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String filename;
    private String storagePath; // Path on the server's file system
    private LocalDateTime uploadedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;
    
    @OneToMany(mappedBy = "studyFile", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ChatMessage> chatMessages;

    @Lob // For storing large text content
    @Column(columnDefinition = "LONGTEXT")
    private String extractedText;
    
    public Long getId() {
    	return id;
    }
    
    public void setId(Long id) {
    	this.id = id;
    }
    
    public String getFilename() {
    	return filename;
    }
    
    public void setFilename(String filename) {
    	this.filename = filename;
    }
    
    public String getStoragePath() {
    	return storagePath;
    }
    
    public void setStoragePath(String storagePath) {
    	this.storagePath = storagePath;
    }
    
    public LocalDateTime getUploadedAt() {
    	return uploadedAt;
    }
    
    public void setUploadedAt(LocalDateTime uploadedAt) {
    	this.uploadedAt = uploadedAt;
    }
    
    public User getUser() {
    	return user;
    }
    
    public void setUser(User user) {
    	this.user = user;
    }
    
    public String getExtractedText() {
    	return extractedText;
    }
    
    public void setExtractedText(String extractedText) {
    	this.extractedText = extractedText;
    }
    
    public List<ChatMessage> getChatMessages(){
    	return chatMessages;
    }
    
    public void setCharMessages(List<ChatMessage> chatMessages) {
    	this.chatMessages = chatMessages;
    }
}
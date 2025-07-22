package com.example.smartstudy.controller;

import com.example.smartstudy.model.ChatMessage;
import com.example.smartstudy.model.StudyFile;
import com.example.smartstudy.model.User;
import com.example.smartstudy.repository.ChatMessageRepository;
import com.example.smartstudy.repository.StudyFileRepository;
import com.example.smartstudy.repository.UserRepository;
import com.example.smartstudy.service.FileStorageService;
import com.example.smartstudy.service.GeminiService;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Comparator;

@Controller
public class DashboardController {

    @Autowired private UserRepository userRepository;
    @Autowired private StudyFileRepository studyFileRepository;
    @Autowired private ChatMessageRepository chatMessageRepository;
    @Autowired private FileStorageService fileStorageService;
    @Autowired private GeminiService geminiService;

    private User getCurrentUser(Authentication authentication) {
        return userRepository.findByEmail(authentication.getName()).orElseThrow(() -> new IllegalStateException("User not found"));
    }

    @GetMapping("/dashboard")
    public String dashboard(Model model, Authentication authentication) {
        User user = getCurrentUser(authentication);
        model.addAttribute("files", studyFileRepository.findByUserId(user.getId()));
        return "dashboard";
    }

    @PostMapping("/upload")
    public String uploadFile(@RequestParam("file") MultipartFile file, Authentication authentication, RedirectAttributes attributes) {
        if (file.isEmpty() || !file.getContentType().equals("application/pdf")) {
            attributes.addFlashAttribute("message", "Please select a PDF file to upload.");
            return "redirect:/dashboard";
        }
        try {
            User user = getCurrentUser(authentication);
            String filePath = fileStorageService.storeFile(file);
            String text = fileStorageService.extractTextFromPdf(filePath);

            StudyFile studyFile = new StudyFile();
            studyFile.setFilename(file.getOriginalFilename());
            studyFile.setStoragePath(filePath);
            studyFile.setUploadedAt(LocalDateTime.now());
            studyFile.setUser(user);
            studyFile.setExtractedText(text);
            studyFileRepository.save(studyFile);
            attributes.addFlashAttribute("message", "File uploaded successfully!");
        } catch (IOException e) {
            attributes.addFlashAttribute("message", "Failed to upload or process file: " + e.getMessage());
        }
        return "redirect:/dashboard";
    }

    @GetMapping("/chat/{fileId}")
    public String chatWithFile(@PathVariable Long fileId, Model model, Authentication authentication) {
        User user = getCurrentUser(authentication);
        StudyFile studyFile = studyFileRepository.findById(fileId).orElseThrow(() -> new IllegalArgumentException("Invalid file Id:" + fileId));

        if (!studyFile.getUser().getId().equals(user.getId())) {
             return "redirect:/dashboard"; // Not authorized
        }

        studyFile.getChatMessages().sort(Comparator.comparing(ChatMessage::getCreatedAt));
        model.addAttribute("file", studyFile);
        return "chat";
    }
    
 // Find the existing askQuestion method and replace it with this one.
    @PostMapping("/chat/{fileId}/ask")
    public String askQuestion(@PathVariable Long fileId, @RequestParam String question, Authentication authentication, RedirectAttributes attributes) {
        User user = getCurrentUser(authentication);
        StudyFile studyFile = studyFileRepository.findById(fileId).orElseThrow(() -> new IllegalArgumentException("Invalid file Id:" + fileId));
         if (!studyFile.getUser().getId().equals(user.getId())) {
             return "redirect:/dashboard"; // Not authorized
        }

        // Save user's question first
        ChatMessage userMessage = new ChatMessage();
        userMessage.setContent(question);
        userMessage.setSender("USER");
        userMessage.setCreatedAt(LocalDateTime.now());
        userMessage.setStudyFile(studyFile);
        chatMessageRepository.save(userMessage);

        try {
            // Ask Gemini
            String prompt = "Context: \"" + studyFile.getExtractedText() + "\". Based on this context, answer the question: " + question;
            String geminiRawResponse = geminiService.generateContent(prompt).block();

            // Process and save Gemini's response
            String geminiAnswer = parseGeminiResponse(geminiRawResponse);
            ChatMessage geminiMessage = new ChatMessage();
            geminiMessage.setContent(geminiAnswer);
            geminiMessage.setSender("GEMINI");
            geminiMessage.setCreatedAt(LocalDateTime.now());
            geminiMessage.setStudyFile(studyFile);
            chatMessageRepository.save(geminiMessage);

        } catch (Exception e) {
            // --- THIS IS THE NEW, ROBUST ERROR HANDLING ---
            // If the API call fails (e.g., bad API key), this will catch it.
            System.err.println("GEMINI API ERROR: " + e.getMessage());
            attributes.addFlashAttribute("errorMessage", "Error calling AI service. Check your API Key and server logs.");
            
            // We still redirect, but now with an error message for the user.
            return "redirect:/chat/" + fileId;
        }

        return "redirect:/chat/" + fileId;
    }
    
    // Helper to parse the complex JSON response from Gemini
    private String parseGeminiResponse(String jsonResponse) {
        try {
            JsonObject root = JsonParser.parseString(jsonResponse).getAsJsonObject();
            JsonArray candidates = root.getAsJsonArray("candidates");
            if (candidates != null && !candidates.isJsonNull() && candidates.size() > 0) {
                JsonObject content = candidates.get(0).getAsJsonObject().getAsJsonObject("content");
                JsonArray parts = content.getAsJsonArray("parts");
                if (parts != null && !parts.isJsonNull() && parts.size() > 0) {
                    return parts.get(0).getAsJsonObject().get("text").getAsString();
                }
            }
        } catch (Exception e) {
            // Log the error
            System.err.println("Error parsing Gemini response: " + e.getMessage());
            return "Sorry, I couldn't process the response.";
        }
        return "No content found in response.";
    }
}
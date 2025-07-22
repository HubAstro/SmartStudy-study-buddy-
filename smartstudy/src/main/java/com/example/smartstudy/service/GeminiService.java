package com.example.smartstudy.service;

import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import org.springframework.web.util.UriComponentsBuilder;
import java.net.URI;

@Service
public class GeminiService {

    private final WebClient webClient;

    // We no longer read from application.properties in this class
    public GeminiService(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.build();
    }

    public Mono<String> generateContent(String prompt) {
        // --- THIS IS THE DIAGNOSTIC CHANGE ---
        // We are hardcoding the values here to test.
        // Bypasses @Value and application.properties completely.
        String hardcodedApiUrl = "https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash-latest:generateContent";
        
        // IMPORTANT: PASTE YOUR ACTUAL API KEY HERE
        String hardcodedApiKey = "PASTE YOUR ACTUAL API KEY HERE"; 

        String requestBody = String.format(
            "{\"contents\":[{\"parts\":[{\"text\": \"%s\"}]}]}",
            prompt.replace("\"", "\\\"").replace("\n", "\\n")
        );

        // Build the URI from the hardcoded values
        URI uri = UriComponentsBuilder.fromHttpUrl(hardcodedApiUrl)
                .queryParam("key", hardcodedApiKey)
                .build()
                .toUri();

        return webClient.post()
                .uri(uri)
                .header("Content-Type", "application/json")
                .bodyValue(requestBody)
                .retrieve()
                .bodyToMono(String.class);
    }
}

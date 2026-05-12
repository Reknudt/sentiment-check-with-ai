package com.pavlov.sentiment.controller;

import com.pavlov.sentiment.model.SentimentRequest;
import com.pavlov.sentiment.model.SentimentResponse;
import com.pavlov.sentiment.model.SentimentResponseDto;
import com.pavlov.sentiment.service.SentimentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.springframework.http.HttpStatus.CREATED;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/sentiment")
public class SentimentController {

    @Value("${sentiment.ai.default-strategy:huggingface}")
    private String defaultStrategy;

    private final SentimentService sentimentService;

    @GetMapping
    public List<SentimentResponse> getAll() {
        return sentimentService.findAll();
    }

    @GetMapping("/ids")
    public Set<String> getAllIds() {
        return sentimentService.findAllIds();
    }

    @GetMapping("/by-text")
    public SentimentResponse getSentimentByText(@RequestParam("text") String text) {
        return sentimentService.findByText(text);
    }

    @GetMapping("/{id}")
    public SentimentResponse getSentimentById(@PathVariable("id") String id) {
        return sentimentService.findById(id);
    }

    @GetMapping("/strategies")
    public ResponseEntity<Map<String, Object>> getAvailableStrategies() {
        List<String> strategies = sentimentService.getAvailableStrategies();
        Map<String, Object> response = new HashMap<>();
        response.put("availableStrategies", strategies);
        response.put("default", defaultStrategy);
        response.put("note", "Use POST /strategyName={strategy} to use specific strategy");
        return ResponseEntity.ok(response);
    }

    @PostMapping
    @ResponseStatus(CREATED)
    public ResponseEntity<SentimentResponseDto> processMessage(@Valid @RequestBody SentimentRequest messageRequest,
                                                               @RequestParam(required = false) String strategyName) throws NoSuchAlgorithmException {
        strategyName = (strategyName != null && !strategyName.trim().isEmpty()) ? strategyName : defaultStrategy;
        SentimentResponseDto responseDto = sentimentService.analyzeText(messageRequest.text(), strategyName);
        return ResponseEntity.ok()
                .header("X-Strategy", strategyName)
                .header("X-Cache", responseDto.isFromCache() ? "HIT" : "MISS")
                .body(responseDto);
    }
}
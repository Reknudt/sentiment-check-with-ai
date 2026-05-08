package com.pavlov.sentiment.controller;

import com.pavlov.sentiment.model.SentimentRequest;
import com.pavlov.sentiment.model.SentimentResponse;
import com.pavlov.sentiment.service.SentimentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
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
import java.util.List;
import java.util.Set;

import static org.springframework.http.HttpStatus.CREATED;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/sentiment")
public class SentimentController {

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

   @PostMapping
   @ResponseStatus(CREATED)
   public ResponseEntity<SentimentResponse> processMessage(@Valid @RequestBody SentimentRequest messageRequest) throws NoSuchAlgorithmException {
       SentimentResponse response = sentimentService.analyzeText(messageRequest.text());
       return ResponseEntity.ok().body(response);
   }
}
package com.pavlov.sentiment.controller;

import com.pavlov.sentiment.model.SentimentRequest;
import com.pavlov.sentiment.model.SentimentResponse;
import com.pavlov.sentiment.service.SentimentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.security.NoSuchAlgorithmException;

import static org.springframework.http.HttpStatus.CREATED;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/sentiment")
public class SentimentController {

   private final SentimentService sentimentService;

   @PostMapping
   @ResponseStatus(CREATED)
   public ResponseEntity<SentimentResponse> processMessage(@Valid @RequestBody SentimentRequest messageRequest) throws NoSuchAlgorithmException {
//       SentimentResponse sentimentResponse = sentimentService.processMessage(messageRequest);

//       log.info("Received analysis request: {}", truncate(request.getText()));

       SentimentResponse response = sentimentService.analyzeText(messageRequest.text());

       // Добавляем заголовок, чтобы клиент знал, был ли ответ из кэша
//       if (response.isFromCache()) {
//           return ResponseEntity.ok()
//                   .header("X-Cache", "HIT")
//                   .body(response);
//       } else {
           return ResponseEntity.ok()
                   .header("X-Cache", "MISS")
                   .body(response);
   }

//   @GetMapping("/{id}")
//   public ResponseEntity<SentimentLog> getMessage(@PathVariable("id") String id) {
//       return ResponseEntity.ok(sentimentService.getMessage(id));
//   }
//
//    @GetMapping()
//    public List<SentimentLog> getMessages() {
//        return sentimentService.getMessages();
//    }
}
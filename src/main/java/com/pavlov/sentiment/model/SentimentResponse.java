package com.pavlov.sentiment.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public record SentimentResponse (String id, @NotBlank String text, @NotBlank String sentiment, @NotNull Double score,
                                 long processingTimeMs, boolean fromCache, LocalDateTime createdAt) {

    public SentimentResponse(String id, String text, String sentiment, Double score, long processingTimeMs) {
        this(id, text, sentiment, score, processingTimeMs, false, LocalDateTime.now());
    }
}

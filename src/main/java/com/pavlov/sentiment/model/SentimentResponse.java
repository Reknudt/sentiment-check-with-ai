package com.pavlov.sentiment.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public record SentimentResponse(String id, String text, @NotBlank String sentiment, @NotNull Double score,
                                 long processingTimeMs, LocalDateTime createdAt) {

    public SentimentResponse(String text, String sentiment, double score, long processingTimeMs) {
        this(null, text, sentiment, score, processingTimeMs, LocalDateTime.now());
    }

    public SentimentResponse(String neutral, double v, long processingTimeMs) {
        this(null, null, neutral, v, processingTimeMs, LocalDateTime.now());
    }
}

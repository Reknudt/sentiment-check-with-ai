package com.pavlov.sentiment.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record SentimentResponse (String id, @NotBlank String sentiment, @NotNull Double score, long processingTimeMs) {

    public SentimentResponse(String sentiment, Double score, long processingTimeMs) {
        this(null, sentiment, score, processingTimeMs);
    }
}

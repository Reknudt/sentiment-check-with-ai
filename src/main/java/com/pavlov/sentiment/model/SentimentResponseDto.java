package com.pavlov.sentiment.model;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

public record SentimentResponseDto(@Valid @NotNull SentimentResponse sentimentResponse, @NotNull Boolean isFromCache) {}

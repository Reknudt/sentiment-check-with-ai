package com.pavlov.sentiment.model;

import jakarta.validation.constraints.NotBlank;

public record SentimentRequest(@NotBlank String text, String lang, AiType aiType) {}

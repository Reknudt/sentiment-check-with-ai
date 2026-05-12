package com.pavlov.sentiment.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class HuggingFaceResponse {
    private String label;      // "POSITIVE", "NEGATIVE"
    private double score;      // confidence 0.0-1.0
}
package com.pavlov.sentiment.strategy;

import com.pavlov.sentiment.model.HuggingFaceResponse;
import com.pavlov.sentiment.model.SentimentResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Duration;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
public class HuggingFaceStrategy implements SentimentStrategy {

    private static final String DEFAULT_MODEL = "distilbert-base-uncased-finetuned-sst-2-english";
    private static final double NEUTRAL_THRESHOLD = 0.6;

    private final WebClient webClient;
    private final String apiKey;
    private final String modelName;

    public HuggingFaceStrategy(
            @Value("${sentiment.ai.huggingface.api-key:}") String apiKey,
            @Value("${sentiment.ai.huggingface.model:" + DEFAULT_MODEL + "}") String modelName,
            @Value("${sentiment.ai.huggingface.api-url:https://api-inference.huggingface.co/models/}") String apiUrl) {

        this.apiKey = apiKey;
        this.modelName = modelName;

        this.webClient = WebClient.builder()
                .baseUrl(apiUrl + modelName)
                .defaultHeader("Authorization", "Bearer " + apiKey)
                .defaultHeader("Content-Type", "application/json")
                .build();
    }

    @Override
    public SentimentResponse analyze(String text) {
        long startTime = System.currentTimeMillis();

        try {
            // Вызов Hugging Face API
            List<HuggingFaceResponse> responses = webClient.post()
                    .bodyValue(Map.of("inputs", text))
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<List<HuggingFaceResponse>>() {})
                    .block(Duration.ofSeconds(10)); // Таймаут 10 секунд

            if (responses == null || responses.isEmpty()) {
                log.warn("Empty response from Hugging Face API");
                return createFallbackResponse(startTime);
            }

            // Обработка ответа
            HuggingFaceResponse topResult = responses.getFirst();
            String sentiment = mapSentiment(topResult);
            double score = topResult.getScore();

            long processingTime = System.currentTimeMillis() - startTime;
            log.info("HuggingFace analysis completed in {} ms: {} (score: {})", processingTime, sentiment, score);
            return new SentimentResponse(text, sentiment, score, processingTime);
        } catch (Exception e) {
            log.error("Hugging Face API error: {}", e.getMessage(), e);
            return createFallbackResponse(startTime);
        }
    }

    private String mapSentiment(HuggingFaceResponse response) {
        String label = response.getLabel().toUpperCase();
        double score = response.getScore();

        if (score < NEUTRAL_THRESHOLD)
            return "NEUTRAL";

        if (label.contains("POSITIVE")) {
            return "POSITIVE";
        } else if (label.contains("NEGATIVE")) {
            return "NEGATIVE";
        }

        return "NEUTRAL";
    }

    private SentimentResponse createFallbackResponse(long startTime) {
        long processingTime = System.currentTimeMillis() - startTime;
        return new SentimentResponse("NEUTRAL", 0.5, processingTime);
    }

    @Override
    public String getStrategyName() {
        return "HuggingFace (" + modelName + ")";
    }

    @Override
    public boolean isAvailable() {
        return apiKey != null && !apiKey.isEmpty();
    }
}
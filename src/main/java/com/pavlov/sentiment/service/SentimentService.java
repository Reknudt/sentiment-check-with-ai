package com.pavlov.sentiment.service;

import com.pavlov.sentiment.model.SentimentResponse;
import com.pavlov.sentiment.model.SentimentResponseDto;
import com.pavlov.sentiment.repository.SentimentCacheRepository;
import com.pavlov.sentiment.strategy.SentimentStrategyFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class SentimentService {

    private final SentimentCacheRepository cacheRepository;

    private final SentimentStrategyFactory strategyFactory;

    public List<SentimentResponse> findAll() {
        return cacheRepository.getAll();
    }

    public Set<String> findAllIds() {
        return cacheRepository.getAllIds();
    }

    public SentimentResponse findByText(String text) {
        return cacheRepository.getByText(text).orElseThrow(
                () -> new IllegalArgumentException(String.format("Message with text %s ... not found", text.length() > 15 ? text.substring(0, 15) : text)));
    }

    public SentimentResponse findById(String id) {
        return cacheRepository.getById(id).orElseThrow(
                () -> new IllegalArgumentException(String.format("Message with id %s not found", id)));
    }

    public SentimentResponseDto analyzeText(String text, String strategyName) {
        long startTime = System.currentTimeMillis();
        SentimentResponse cachedResponse = cacheRepository.getByText(text).orElse(null);
        if (cachedResponse != null) {
            log.info("Cache HIT for text: {}", cacheRepository.truncate(text));
            return new SentimentResponseDto(cachedResponse, true);
        }
        var strategy = strategyName == null ? strategyFactory.getDefaultStrategy() : strategyFactory.getStrategy(strategyName);
        SentimentResponse response = strategy.analyze(text);
        cacheRepository.save(text, response);
        long totalTime = System.currentTimeMillis() - startTime;
        log.info("Analysis completed in {} ms using {}", totalTime, strategyName);
        return new SentimentResponseDto(response, false);
    }

    public List<String> getAvailableStrategies() {
        return strategyFactory.getAvailableStrategies();
    }
}
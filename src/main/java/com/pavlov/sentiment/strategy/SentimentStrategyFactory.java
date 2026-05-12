package com.pavlov.sentiment.strategy;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
@RequiredArgsConstructor
public class SentimentStrategyFactory {
    
    private final Map<String, SentimentStrategy> strategies = new ConcurrentHashMap<>();
    
    @Value("${sentiment.ai.default-strategy:huggingface}")
    private String defaultStrategy;
    
    private final List<SentimentStrategy> strategyList;
    
    @PostConstruct
    public void init() {
        for (SentimentStrategy strategy : strategyList) {
            strategies.put(strategy.getStrategyName().toLowerCase(), strategy);
            log.info("Registered sentiment strategy: {} (available: {})", strategy.getStrategyName(), strategy.isAvailable());
        }
    }
    
    /**
     * Получить стратегию по имени
     */
    public SentimentStrategy getStrategy(String name) {
        String key = name.toLowerCase();
        SentimentStrategy strategy = strategies.get(key);
        
        if (strategy == null) {
            log.warn("Strategy '{}' not found, using default: {}", name, defaultStrategy);
            strategy = strategies.get(defaultStrategy.toLowerCase());
        }
        if (strategy == null)
            throw new IllegalStateException("No sentiment strategy available!");
        if (!strategy.isAvailable()) {
            log.warn("Strategy '{}' is not available (missing credentials/config)", name);
            // Ищем доступную стратегию
            strategy = strategies.values().stream()
                    .filter(SentimentStrategy::isAvailable)
                    .findFirst()
                    .orElse(strategy);
        }
        return strategy;
    }
    
    /**
     * Получить стратегию по умолчанию
     */
    public SentimentStrategy getDefaultStrategy() {
        return getStrategy(defaultStrategy);
    }
    
    /**
     * Получить все доступные стратегии
     */
    public List<String> getAvailableStrategies() {
        return strategies.values().stream()
                .filter(SentimentStrategy::isAvailable)
                .map(SentimentStrategy::getStrategyName)
                .toList();
    }
}
package com.pavlov.sentiment.strategy;

import com.pavlov.sentiment.model.SentimentResponse;

public interface SentimentStrategy {    //todo add another two ai models

    /**
     * Анализирует тональность текста
     * @param text текст для анализа
     * @return результат анализа
     */
    SentimentResponse analyze(String text);

    /**
     * Возвращает название стратегии
     */
    String getStrategyName();

    /**
     * Проверяет, доступна ли модель
     */
    boolean isAvailable();
}
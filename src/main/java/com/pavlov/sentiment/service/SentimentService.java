package com.pavlov.sentiment.service;

import com.pavlov.sentiment.model.SentimentResponse;
import com.pavlov.sentiment.repository.SentimentCacheRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class SentimentService {

    private final SentimentCacheRepository sentimentCacheRepository;

    public List<SentimentResponse> findAll() {
        return sentimentCacheRepository.getAll();
    }

    public Set<String> findAllIds() {
        return sentimentCacheRepository.getAllIds();
    }

    public SentimentResponse findByText(String text) {
        return sentimentCacheRepository.getByText(text).orElseThrow(
                () -> new IllegalArgumentException(String.format("Message with text %s ... not found", text.length() > 15 ? text.substring(0, 15) : text)));
    }

    public SentimentResponse findById(String id) {
        return sentimentCacheRepository.getById(id).orElseThrow(
                () -> new IllegalArgumentException(String.format("Message with id %s not found", id)));
    }

    /**
     * Анализирует тональность текста
     * Сначала проверяет кэш, если нет - вызывает "дорогой" анализ
     */
    public SentimentResponse analyzeText(String text) throws NoSuchAlgorithmException {
        long startTime = System.currentTimeMillis();
        // Позже на реальный вызов ML модели
        SentimentResponse response = mockAnalysis(text);
        sentimentCacheRepository.save(text, response);
        long totalTime = System.currentTimeMillis() - startTime;
        log.info("Analysis completed in {} ms", totalTime);
        return response;
    }

    /**
     * ЗАГЛУШКА: Имитирует медленный анализ
     * Позже заменим на реальную ML модель
     */
    private SentimentResponse mockAnalysis(String text) throws NoSuchAlgorithmException {
        try {
            // Имитируем тяжелую работу (1 секунда)
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // Примитивная логика для демонстрации
        String lowerText = text.toLowerCase();
        String sentiment;
        double confidence;

        if (lowerText.contains("love") || lowerText.contains("good") || lowerText.contains("great")) {
            sentiment = "POSITIVE";
            confidence = 0.85;
        } else if (lowerText.contains("hate") || lowerText.contains("bad") || lowerText.contains("terrible")) {
            sentiment = "NEGATIVE";
            confidence = 0.82;
        } else {
            sentiment = "NEUTRAL";
            confidence = 0.60;
        }
        String key = generateKey(text);
        return new SentimentResponse(key, text, sentiment, confidence, 1000);
    }

    private String generateKey(String text) throws NoSuchAlgorithmException {
        MessageDigest md5 = MessageDigest.getInstance("MD5");
        if (text.length() < 200) {
            md5.update(text.getBytes(StandardCharsets.UTF_8));
        } else {
            md5.update(text.substring(0, 200).getBytes(StandardCharsets.UTF_8));
        }
        byte[] digest = md5.digest();
        StringBuilder hash = new StringBuilder();
        for (byte b : digest) {
            hash.append(String.format("%02x", b));
        }
        return hash.toString();
    }
}
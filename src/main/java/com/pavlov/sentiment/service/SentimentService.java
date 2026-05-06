package com.pavlov.sentiment.service;

import com.pavlov.sentiment.model.SentimentResponse;
import com.pavlov.sentiment.repository.SentimentCacheRepository;
//import com.pavlov.sentiment.repository.SentimentLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SentimentService {

//    private final SentimentLogRepository sentimentLogRepository;

    private final SentimentCacheRepository sentimentCacheRepository;

//    public List<SentimentLog> getMessages() {
//        return (List<SentimentLog>) sentimentLogRepository.findAll();
//    }

//    public SentimentLog getMessage(String id) {
//        return sentimentLogRepository.findById(id).orElseThrow(() -> new IllegalArgumentException(String.format("Message with key %s not found", id)));
//    }

//    public SentimentResponse processMessage(MessageRequest messageRequest) throws NoSuchAlgorithmException {
//        MessageDigest md5 = MessageDigest.getInstance("MD5");
//        md5.update(messageRequest.text().substring(0, 200).getBytes(StandardCharsets.UTF_8));
//        byte[] digest = md5.digest();
//        StringBuilder hash = new StringBuilder();
//        for (byte b : digest) {
//            hash.append(String.format("%02x", b));
//        }
//        String id = sentimentLogRepository.save(new SentimentLog(hash.toString(), messageRequest.text())).key();
// //        String id = sentimentLogRepository.save(new SentimentLog(hash.toString(), messageRequest.data())).getKey();
//
//        return new SentimentResponse(id, "neutral", 10.0, 500);
//    }

    //------

    /**
     * Анализирует тональность текста
     * Сначала проверяет кэш, если нет - вызывает "дорогой" анализ
     */
    public SentimentResponse analyzeText(String text) {
        long startTime = System.currentTimeMillis();

        // ШАГ 1: Проверяем Redis кэш
        SentimentResponse cachedResponse = (SentimentResponse) sentimentCacheRepository.get(text);

        if (cachedResponse != null) {
            // Нашли в кэше! Возвращаем мгновенно
//            log.info(" Cache HIT for text: {}", truncate(text));
//            cachedResponse.setFromCache(true);
            return cachedResponse;
        }

        // ШАГ 2: В кэше нет - делаем "дорогой" анализ
//        log.info("Cache MISS for text: {}, performing analysis...", truncate(text));

        // ЗДЕСЬ ПОКА ЗАГЛУШКА
        // Позже заменим на реальный вызов ML модели
        SentimentResponse response = mockAnalysis(text);

        // ШАГ 3: Сохраняем результат в кэш
        sentimentCacheRepository.save(text, response);

        long totalTime = System.currentTimeMillis() - startTime;
//        log.info("Analysis completed in {} ms", totalTime);

        return response;
    }

    /**
     * ЗАГЛУШКА: Имитирует медленный анализ
     * Позже заменим на реальную ML модель
     */
    private SentimentResponse mockAnalysis(String text) {
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

        return new SentimentResponse(sentiment, confidence, 1000);
    }

    private String truncate(String text) {
        return text.length() > 50 ? text.substring(0, 47) + "..." : text;
    }
}
package com.pavlov.sentiment.repository;

import com.pavlov.sentiment.model.SentimentResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.stereotype.Repository;
import tools.jackson.databind.ObjectMapper;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Slf4j
@Repository
public class SentimentCacheRepository {

    @Value("${spring.data.redis.dataPrefix}")
    private final String DATA_PREFIX;

    @Value("${spring.data.redis.textPrefix}")
    private final String TEXT_PREFIX;

    @Value("${spring.data.redis.cache.timeToLive}")
    private final long CACHE_TTL_HOURS;

    private final RedisTemplate<String, Object> redisTemplate;

    public SentimentCacheRepository(@Value("${spring.data.redis.dataPrefix}") String DATA_PREFIX,
                                    @Value("${spring.data.redis.textPrefix}") String TEXT_PREFIX,
                                    @Value("${spring.data.redis.cache.timeToLive}") long CACHE_TTL_HOURS,
                                    RedisTemplate<String, Object> redisTemplate) {
        this.DATA_PREFIX = DATA_PREFIX;
        this.TEXT_PREFIX = TEXT_PREFIX;
        this.CACHE_TTL_HOURS = CACHE_TTL_HOURS;
        this.redisTemplate = redisTemplate;
    }

    /**
     * Сохраняет результат анализа в Redis
     * @param text Исходный текст (используется для генерации ключа)
     * @param response Результат анализа
     */
    public void save(String text, SentimentResponse response) {
        String dataKey = DATA_PREFIX + response.id();
        String textIndexKey = TEXT_PREFIX + generateKey(text);
        redisTemplate.opsForValue().set(dataKey, response, CACHE_TTL_HOURS, TimeUnit.HOURS);
        redisTemplate.opsForValue().set(textIndexKey, response.id(), CACHE_TTL_HOURS, TimeUnit.HOURS);
        log.info("Saved: ID={}, Text={}", response.id(), truncate(text));
    }

    /**
     * Получение по тексту (через индекс)
     */
    public Optional<SentimentResponse> getByText(String text) {
        String textIndexKey = TEXT_PREFIX + generateKey(text);
        String id = (String) redisTemplate.opsForValue().get(textIndexKey);
        if (id == null) {
            log.info("SentimentResponse not found");
            return Optional.empty();
        }
        log.info("Found SentimentResponse by text, id is : ={}, Text={}", id, truncate(text));
        return getById(id);
    }

    /**
     * Прямое получение по ID (основной метод)
     */
    public Optional<SentimentResponse> getById(String id) {
        String dataKey = DATA_PREFIX + id;
        RedisSerializer<?> originalValueSerializer = redisTemplate.getValueSerializer();
        try {
            redisTemplate.setValueSerializer(new StringRedisSerializer());
            String json = (String) redisTemplate.opsForValue().get(dataKey);
            if (json == null)
                return Optional.empty();
            ObjectMapper objectMapper = new ObjectMapper();
            SentimentResponse response = objectMapper.readValue(json, SentimentResponse.class);
            return Optional.of(response);
        } catch (Exception e) {
            log.error("Failed to deserialize: {}", e.getMessage());
            return Optional.empty();
        } finally {
            redisTemplate.setValueSerializer(originalValueSerializer);
        }
    }

    /**
     * Получение всех ID (для администрирования)
     */
    public Set<String> getAllIds() {
        // Используем SCAN вместо KEYS
        Set<String> keys = redisTemplate.keys(DATA_PREFIX + "*");
        if (keys == null || keys.isEmpty())
            return Collections.emptySet();
        return keys.stream().map(key -> key.substring(DATA_PREFIX.length())).collect(Collectors.toSet());
    }

    /**
     * Получение всех данных (осторожно, может быть много!)
     */
    public List<SentimentResponse> getAll() {
        return getAllIds().stream().map(this::getById).filter(Optional::isPresent).map(Optional::get).toList();
    }

    public String truncate(String text) {
        return text.length() > 50 ? text.substring(0, 50) + "..." : text;
    }

    /**
     * Генерирует уникальный ключ для текста.
     * Используем MD5, чтобы:
     * 1. Ключ был фиксированной длины
     * 2. Избежать проблем с длинными текстами
     * 3. Одинаковые тексты давали одинаковый ключ
     */
    private String generateKey(String text) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] hash = md.digest(text.getBytes());
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                hexString.append(String.format("%02x", b));
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            log.warn("MD5 not available, using hashCode as fallback");
            return String.valueOf(text.hashCode());
        }
    }
}
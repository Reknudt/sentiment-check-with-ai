package com.pavlov.sentiment.repository;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.TimeUnit;

@Slf4j
@Repository
public class SentimentCacheRepository {
    
    private static final String CACHE_KEY_PREFIX = "sentiment:";  // Префикс для всех ключей
    private static final long CACHE_TTL_HOURS = 24;  // Храним в кэше 24 часа
    
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;
    
    /**
     * Сохраняет результат анализа в Redis
     * @param text Исходный текст (используется для генерации ключа)
     * @param response Результат анализа
     */
    public void save(String text, Object response) {
        String key = generateKey(text);
        try {
            // Сохраняем в Redis с TTL 24 часа
            redisTemplate.opsForValue().set(key, response, CACHE_TTL_HOURS, TimeUnit.HOURS);
            log.info("Saved to Redis cache. Key: {}, TTL: {} hours", key, CACHE_TTL_HOURS);
        } catch (Exception e) {
            log.error("Failed to save to Redis: {}", e.getMessage(), e);
        }
    }
    
    /**
     * Получает результат из Redis
     * @param text Исходный текст
     * @return Результат или null, если не найден
     */
    public Object get(String text) {
        String key = generateKey(text);
        try {
            Object value = redisTemplate.opsForValue().get(key);
            if (value != null) {
                log.info("Retrieved from Redis cache. Key: {}", key);
            } else {
                log.debug("Cache miss for key: {}", key);
            }
            return value;
        } catch (Exception e) {
            log.error("Failed to get from Redis: {}", e.getMessage(), e);
            return null;
        }
    }
    
    /**
     * Проверяет, есть ли результат в кэше
     */
    public boolean exists(String text) {
        String key = generateKey(text);
        return redisTemplate.hasKey(key);
    }
    
    /**
     * Удаляет запись из кэша (полезно для инвалидации)
     */
    public void delete(String text) {
        String key = generateKey(text);
        redisTemplate.delete(key);
        log.info("Deleted from Redis cache. Key: {}", key);
    }
    
    /**
     * Генерирует уникальный ключ для текста
     * Используем MD5, чтобы:
     * 1. Ключ был фиксированной длины
     * 2. Избежать проблем с длинными текстами
     * 3. Одинаковые тексты давали одинаковый ключ
     */
    private String generateKey(String text) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] hash = md.digest(text.getBytes());
            
            // Конвертируем байты в hex строку
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                hexString.append(String.format("%02x", b));
            }
            
            return CACHE_KEY_PREFIX + hexString.toString();
            
        } catch (NoSuchAlgorithmException e) {
            // Fallback: используем hashCode если MD5 недоступен
            log.warn("MD5 not available, using hashCode as fallback");
            return CACHE_KEY_PREFIX + text.hashCode();
        }
    }
}
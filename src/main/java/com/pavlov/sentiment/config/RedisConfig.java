package com.pavlov.sentiment.config;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.JacksonJsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import tools.jackson.databind.DefaultTyping;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.jsontype.BasicPolymorphicTypeValidator;
import tools.jackson.databind.jsontype.PolymorphicTypeValidator;


@Configuration
public class RedisConfig {

    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory) {

        // 1. Создаем шаблон для работы с Redis
        RedisTemplate<String, Object> template = new RedisTemplate<>();

        // 2. Устанавливаем фабрику соединений (как подключаться к Redis)
        template.setConnectionFactory(connectionFactory);

        // 3. Настраиваем сериализаторы (как сохранять объекты в Redis)

        // 3.1. Для ключей используем строки (просто и понятно)
        StringRedisSerializer stringSerializer = new StringRedisSerializer();
        template.setKeySerializer(stringSerializer);
        template.setHashKeySerializer(stringSerializer);

        // 3.2. Для значений используем JSON (чтобы хранить наши объекты)
        JacksonJsonRedisSerializer<Object> jsonSerializer = createJsonSerializer();
        template.setValueSerializer(jsonSerializer);
        template.setHashValueSerializer(jsonSerializer);

        // 4. Инициализируем шаблон (применяем настройки)
        template.afterPropertiesSet();

        return template;
    }

    private JacksonJsonRedisSerializer<Object> createJsonSerializer() {

        // Создаем ObjectMapper для настройки JSON сериализации
        PolymorphicTypeValidator ptv = BasicPolymorphicTypeValidator.builder()
                .allowIfSubType("com.sentiment.pavlov.model")
                .allowIfSubType("java.util.ArrayList")
                .allowIfSubType("java.util.HashMap")
                .build();

        ObjectMapper objectMapper = JsonMapper.builder().activateDefaultTyping(
            ptv,
            DefaultTyping.NON_FINAL,
                JsonTypeInfo.As.PROPERTY
        ).build();



        return new JacksonJsonRedisSerializer<>(objectMapper, Object.class);
    }
}


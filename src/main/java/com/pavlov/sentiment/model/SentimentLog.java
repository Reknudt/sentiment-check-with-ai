package com.pavlov.sentiment.model;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;

@RedisHash
public record SentimentLog(@Id @NotBlank String key, @NotBlank String value) {}
//@Getter
//@AllArgsConstructor
//public class SentimentLog{
//    @Id @NotBlank String key;
//    @NotBlank String value;
//}

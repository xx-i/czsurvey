package com.github.czsurvey.extra.data.cache;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.JdkSerializationRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

/**
 * @author YanYu
 */
@AutoConfiguration
public class RedisTemplateConfiguration {

    @Bean
    @Primary
    RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory factory) {
        return createRedisTemplate(factory, new GenericJackson2JsonRedisSerializer());
    }

    @Bean
    RedisTemplate<String, Object> jdkSerializeRedisTemplate(RedisConnectionFactory factory) {
        return createRedisTemplate(factory, new JdkSerializationRedisSerializer());
    }

    private RedisTemplate<String, Object> createRedisTemplate(RedisConnectionFactory factory, RedisSerializer<?> valueSerializer) {
        RedisTemplate<String, Object> redisTemplate = new RedisTemplate<>();
        redisTemplate.setConnectionFactory(factory);
        StringRedisSerializer stringRedisSerializer = new StringRedisSerializer();
        redisTemplate.setKeySerializer(stringRedisSerializer);
        redisTemplate.setHashKeySerializer(stringRedisSerializer);
        redisTemplate.setValueSerializer(valueSerializer);
        redisTemplate.setHashValueSerializer(valueSerializer);
        return redisTemplate;
    }
}

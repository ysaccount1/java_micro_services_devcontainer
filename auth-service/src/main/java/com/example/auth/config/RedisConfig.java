package com.example.auth.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.beans.factory.annotation.Value;
import com.example.auth.model.User;

@Configuration
@EnableCaching
public class RedisConfig {

    @Value("${spring.redis.host}")
    private String redisHost;
    
    @Value("${spring.redis.port}")
    private int redisPort;
    
    @Bean
    public JedisConnectionFactory redisConnectionFactory() {
        try {
            RedisStandaloneConfiguration config = new RedisStandaloneConfiguration(redisHost, redisPort);
            JedisConnectionFactory factory = new JedisConnectionFactory(config);
            factory.afterPropertiesSet();
            return factory;
        } catch (Exception e) {
            // Log the error but return a working factory
            System.err.println("Error creating Redis connection: " + e.getMessage());
            RedisStandaloneConfiguration config = new RedisStandaloneConfiguration("localhost", 6379);
            return new JedisConnectionFactory(config);
        }
    }

    @Bean
    public RedisTemplate<String, User> redisTemplate(RedisConnectionFactory connectionFactory) {
        try {
            RedisTemplate<String, User> template = new RedisTemplate<>();
            template.setConnectionFactory(connectionFactory);
            template.setKeySerializer(new StringRedisSerializer());
            template.setValueSerializer(new Jackson2JsonRedisSerializer<>(User.class));
            template.afterPropertiesSet();
            return template;
        } catch (Exception e) {
            // Log the error but return a basic template
            System.err.println("Error creating Redis template: " + e.getMessage());
            RedisTemplate<String, User> template = new RedisTemplate<>();
            template.setConnectionFactory(connectionFactory);
            return template;
        }
    }
} 
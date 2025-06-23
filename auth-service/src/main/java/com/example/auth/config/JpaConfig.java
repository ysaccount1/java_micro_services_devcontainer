package com.example.auth.config;

import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@Configuration
@EntityScan("com.example.auth.model")
@EnableJpaRepositories("com.example.auth.repository")
public class JpaConfig {
}
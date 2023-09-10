package com.example.demoBot.config;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@Configuration
public class BotConfig {
    @Value("${bot.name}")
    private String name;

    @Value("${bot.token}")
    private String botToken;
    @Value("${bot.owner}")
    private Long botOwner;


    @Bean
    public String botToken() {
        return botToken;
    }
    @Bean
    public Long botOwner() {
        return botOwner;
    }
    @Bean
    @Qualifier("telegramBot")
    public String name() {
        return name;
    }
}

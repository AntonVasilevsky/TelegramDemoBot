package com.example.demoBot.config;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class BotConfig {

    @Value("${bot.token}")
    private String botToken;
    @Value("${bot.owner}")
    private Long botOwner;


    @Bean
    public String getBotToken() {
        return botToken;
    }
    @Bean
    public Long getBotOwner() {
        return botOwner;
    }
}

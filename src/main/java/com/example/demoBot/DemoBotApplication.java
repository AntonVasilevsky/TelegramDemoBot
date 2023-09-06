package com.example.demoBot;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.PropertySource;

@SpringBootApplication
@PropertySource("application.properties")
public class DemoBotApplication {
	@Value("${bot.token}")
	private String botToken;

	public static void main(String[] args) {
		SpringApplication.run(DemoBotApplication.class, args);
	}

	@Bean
	public String getBotToken() {
		return botToken;
	}

}

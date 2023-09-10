package com.example.demoBot;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.PropertySource;

@SpringBootApplication
@PropertySource("application.properties")
public class DemoBotApplication {


	public static void main(String[] args) {

		ConfigurableApplicationContext context = SpringApplication.run(DemoBotApplication.class, args);
		// check bean name creation
		String[] beanDefinitionNames = context.getBeanFactory().getBeanDefinitionNames();
		for (String beanDefinitionName : beanDefinitionNames) {
			System.out.println(beanDefinitionName);
		}
	}



}

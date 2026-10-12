package com.skillforge;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class SkillForgeApplication {

	public static void main(String[] args) {
		String dbUrl = System.getenv("SPRING_DATASOURCE_URL");
		System.out.println("=================================================");
		System.out.println(">>> SkillForge Startup Diagnostics:");
		System.out.println(">>> SPRING_DATASOURCE_URL: " + (dbUrl != null ? dbUrl.replaceAll(":[^@/]+@", ":****@") : "NOT SET (Falling back to localhost:3306)"));
		System.out.println(">>> SPRING_DATASOURCE_USERNAME: " + System.getenv("SPRING_DATASOURCE_USERNAME"));
		System.out.println("=================================================");
		SpringApplication.run(SkillForgeApplication.class, args);
	}

	@Bean
	public ObjectMapper objectMapper() {
		return new ObjectMapper();
	}
}


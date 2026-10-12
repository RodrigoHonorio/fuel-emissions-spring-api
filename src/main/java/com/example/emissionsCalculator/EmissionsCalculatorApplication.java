package com.example.emissionsCalculator;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.web.config.EnableSpringDataWebSupport;
import org.springframework.scheduling.annotation.EnableAsync;

@EnableSpringDataWebSupport
@EnableAsync
@SpringBootApplication
public class EmissionsCalculatorApplication {

	public static void main(String[] args) {
		SpringApplication.run(EmissionsCalculatorApplication.class, args);
	}
}

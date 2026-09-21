package uk.org.spire.emissionsCalculator;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class 	EmissionsCalculatorApplication {

	public static void main(String[] args) {
		SpringApplication.run(EmissionsCalculatorApplication.class, args);
	}

}

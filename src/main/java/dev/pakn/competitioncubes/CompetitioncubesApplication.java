package dev.pakn.competitioncubes;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class CompetitioncubesApplication {
	public static void main(String[] args) {
		SpringApplication.run(CompetitioncubesApplication.class, args);
	}
}

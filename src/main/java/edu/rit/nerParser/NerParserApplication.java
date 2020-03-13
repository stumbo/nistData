package edu.rit.nerParser;

import lombok.extern.log4j.Log4j2;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;

@Log4j2
@EntityScan
@SpringBootApplication
public class NerParserApplication  {
	public static void main(String[] args) {
		SpringApplication.run(NerParserApplication.class);
	}
}

package edu.rit.nerParser;

import lombok.extern.log4j.Log4j2;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;

@Log4j2
@SpringBootApplication
@EntityScan
public class NerParserApplication implements CommandLineRunner {

	@Autowired
	NistData nistData;

	@Autowired
	Parser parser;

	@Autowired
	CsvWriter csvWriter;

	public static void main(String[] args) {
		SpringApplication.run(NerParserApplication.class, args);
	}

	@Override
	public void run(String... args) throws Exception {
		if (args.length != 1) {
			System.out.println("Incorrect Arguments, expected filename:  file.csv");
			return;
		}
		System.out.println("Starting NIST Vulnerability Import.");

		if (!nistData.load()) {
			log.info("Failed to load NIST Data");
			return;
		}
		System.out.println("Starting Individual File Processing, updating database entries.");
		parser.processNISTFiles(nistData.getJsonFiles());

		System.out.println("Starting Name Entity Recognition on new database entries.");
		parser.doNER();

		System.out.println("Writing CSV output.");
		csvWriter.createCSV(args[0]);

		System.out.println("Processing Complete. \n Ending Processing.");

	}
}

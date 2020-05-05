package edu.rit.nerParser;

import edu.rit.nerParser.process.NistProcessor;
import lombok.extern.log4j.Log4j2;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.jms.annotation.EnableJms;

import javax.annotation.PreDestroy;

@Log4j2
@EntityScan
@EnableJms
@SpringBootApplication
public class NerParserApplication implements CommandLineRunner{

	private final NistProcessor nistProcessor;

	public NerParserApplication(NistProcessor nistProcessor) {
		this.nistProcessor = nistProcessor;
	}

	@PreDestroy
	public void onExit() {
		log.info("Stopping NerParserApplication");
		try {
			Thread.sleep(5000);
		} catch (InterruptedException e) {
			log.error("Sleep Interrupted", e);
		}

	}

	public static void main(String[] args) {
		SpringApplication.run(NerParserApplication.class, args);
	}

	/**
	 * Run once and exit
	 *
	 * @param args
	 */
	@Override
	public void run(String... args) {
		if (args.length != 1) {
			System.out.println("Incorrect Arguments, expected filename:  file.csv");
			return;
		}

		log.info("Starting NIST Vulnerability Import.");
		nistProcessor.run(args[0]);
		log.info("Completing NIST Vulnerability Import.");
		System.exit(0);
	}

}

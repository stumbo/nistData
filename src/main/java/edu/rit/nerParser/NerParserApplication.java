package edu.rit.nerParser;

import edu.rit.nerParser.data.repository.DescriptionRepository;
import edu.rit.nerParser.process.CsvWriter;
import lombok.extern.log4j.Log4j2;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.ExitCodeGenerator;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.ApplicationContext;
import org.springframework.jms.annotation.EnableJms;
import org.springframework.jms.core.JmsTemplate;

import java.util.Collections;

@Log4j2
@EntityScan
@EnableJms
@SpringBootApplication
public class NerParserApplication implements CommandLineRunner, ExitCodeGenerator {

	@Autowired
	JobLauncher jobLauncher;

	@Autowired
	@Qualifier("processNistData")
	Job processNistData;

	@Autowired
	private ApplicationContext appContext;

	@Autowired
	private JmsTemplate jmsTemplate;

	@Autowired
	private CsvWriter csvWriter;

	@Autowired
	private DescriptionRepository descriptionRepository;

	public static void main(String[] args) {
		System.exit(SpringApplication.exit(SpringApplication.run(NerParserApplication.class, args)));
	}

	/**
	 * Run once and exit
	 *
	 * @param args
	 * @throws Exception
	 */
	@Override
	public void run(String... args) throws Exception {
		// todo:  set up job parameters correctly
		JobParameters parameters = new JobParameters();
		jobLauncher.run(processNistData, parameters);
		while (getMessageCount() > 0) {
			log.info("Number of messages to left process: {}", this::getMessageCount);
			Thread.sleep(30000);
		}

		// look for any descriptions that don't have NER results.
		// This should be minimal, but is possible.
		descriptionRepository.getAllByNerIsNull().forEach(entry ->
			jmsTemplate.send(session -> session.createTextMessage(entry.getText())));


		// write the results
		csvWriter.createCSV("vulnerabilities.csv");

		// shut down
		System.exit(SpringApplication.exit(appContext, this));
	}

	@Override
	public int getExitCode() {
		return 0;
	}

	public int getMessageCount()
	{
		return jmsTemplate.browse((s, qb) -> Collections.list(qb.getEnumeration()).size());
	}
}

package edu.rit.nerParser;

import lombok.extern.log4j.Log4j2;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.launch.support.CommandLineJobRunner;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.ExitCodeGenerator;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.ApplicationContext;

@Log4j2
@EntityScan
@SpringBootApplication
public class NerParserApplication implements CommandLineRunner, ExitCodeGenerator {

	@Autowired
	JobLauncher jobLauncher;

	@Autowired
	@Qualifier("processNistData")
	Job processNistData;

	@Autowired
	private ApplicationContext appContext;

	public static void main(String[] args) throws Exception {
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
		System.exit(SpringApplication.exit(appContext, this));
	}

	@Override
	public int getExitCode() {
		return 0;
	}
}

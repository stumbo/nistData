package edu.rit.nerParser.batch;

import edu.rit.nerParser.cve.CVEItemService;
import edu.rit.nerParser.process.NistData;
import edu.rit.nerParser.cve.data.CVEItem;
import edu.rit.nerParser.data.VulnerabilityEntity;
import edu.rit.nerParser.data.repository.DescriptionRepository;
import edu.rit.nerParser.data.repository.VulnerabilityRepository;
import lombok.extern.log4j.Log4j2;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobExecutionListener;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.partition.support.Partitioner;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.database.JpaItemWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import javax.jms.Destination;
import javax.persistence.EntityManagerFactory;
import java.io.File;

/**
 * Batch Processing Config
 *
 * Module to setup Batch Processing workflow.
 *
 * Processing starts by updating local copy of NIST Data Files.  Once this has completed
 * a separate processing thread is created for each JSON file.  Each thread reads the
 * JSON input file processes it and writes the output into the MySQL database.
 *
 */
@Log4j2
@EnableBatchProcessing
@Configuration
public class BatchProcessorConfig implements JobExecutionListener{
  private final JobBuilderFactory jobBuilderFactory;
  private final StepBuilderFactory stepBuilderFactory;
  private final EntityManagerFactory entityManagerFactory;
  private final NistData nistData;
  private final Destination writerDestination;
  private final JmsTemplate jmsTemplate;
  private final CVEItemService cveItemService;

  @Autowired
  BatchProcessorConfig(final JobBuilderFactory jobBuilderFactory,
                       final StepBuilderFactory stepBuilderFactory,
                       final EntityManagerFactory entityManagerFactory,
                       final NistData nistData,
                       final Destination writerDestination,
                       final JmsTemplate jmsTemplate,
                       final CVEItemService cveItemService) {
    this.jobBuilderFactory = jobBuilderFactory;
    this.stepBuilderFactory = stepBuilderFactory;
    this.entityManagerFactory = entityManagerFactory;
    this.nistData = nistData;
    this.writerDestination = writerDestination;
    this.jmsTemplate = jmsTemplate;
    this.cveItemService = cveItemService;
  }

  /**
   * The root processing job.
   *   Start - Updates local NIST Data Files
   *   Next - partitions a thread for each JSON file and initiates processing
   *
   * @return
   */
  @Bean(name = "processNistData")
  public Job processNistData() {
    return jobBuilderFactory.get("processNistData")
        .incrementer(new RunIdIncrementer())
        .listener(this)
        .preventRestart()
        .flow(nistDataLoadStep())
        .next(nistDataProcessing())
        .end()
        .build();
  }

  public void beforeJob(JobExecution jobExecution) {
    log.info("Batch Job Started");
  }

  public void afterJob(JobExecution jobExecution){
    log.info("Batch Job Completed.  Completion State:  {}", jobExecution.getStatus());
  }

  /**
   * Job Step that wraps the NIST Data File Update process task
   *
   * @return
   */
  @Bean
  public Step nistDataLoadStep() {
    return stepBuilderFactory.get("nistDataLoadStep")
        .tasklet(loadNistDataTasklet())
        .build();
  }

  /**
   * The NIST Data File Update Task
   *
   * @return
   */
  @Bean
  public Tasklet loadNistDataTasklet() {

    return new NistDataTask(nistData);
  }

  /**
   * Build the execution context for each parallel step.
   * Execution Context contains the dataFile to be processed.
   *
   * @return partitioner
   */
  @Bean("partitioner")
  public Partitioner partitioner() {
    log.info("In Partitioner");
    return new CustomPartitioner(nistData.getJsonFiles());
  }

  /**
   * Setup the Thread Pool to allow multiple instances to run concurrently.
   * Do not place any limitations on the number of tasks that can be
   * queued up.
   *
   * @return
   */
  @Bean
  public ThreadPoolTaskExecutor taskExecutor() {
    ThreadPoolTaskExecutor taskExecutor = new ThreadPoolTaskExecutor();
    taskExecutor.setCorePoolSize(4);
    taskExecutor.afterPropertiesSet();
    return taskExecutor;
  }

  @Bean
  @Qualifier("nistDataProcessing")
  public Step nistDataProcessing() {
    return stepBuilderFactory.get("nistDataProcessing")
        .partitioner("processingSteps", partitioner())
        .step(processingSteps())
        .gridSize(nistData.getJsonFiles().length)
        .taskExecutor(taskExecutor())
        .build();
  }

  /**
   * Template for processing a NIST data file.  A processing Step task is created for each file.  This process
   * reads the data file, extracting each vulnerability description, and writes it as a record in the database,
   * if the vulnerability description does not already exist.
   *
   * @return
   */
  @Bean
  @Qualifier("processingSteps")
  public Step processingSteps() {
    return stepBuilderFactory.get("processingSteps")
        .<CVEItem, VulnerabilityEntity>chunk(1)
        .reader(itemReader(null))
        .processor(itemProcessor())
        .writer(jpaItemWriter())
        .build();
  }

  /**
   * Create the JSON Item Reader, reads the NIST data file extracting one JSON record at a time.
   *
   * @param dataFile
   * @return
   */
  @Bean
  @StepScope
  public ItemReader<CVEItem> itemReader(
      @Value("#{stepExecutionContext['dataFile']}") File dataFile) {
    return new NistItemReader(dataFile);
  }

  /**
   * Create the JSON Item processor.  Processor extracts the vulnerability description from the provided
   * JSON data.
   *
   * @return
   */
  @Bean
  @StepScope
  public ItemProcessor<CVEItem, VulnerabilityEntity> itemProcessor() {

    return new NistItemProcessor(writerDestination,
        jmsTemplate,
        cveItemService);
  }

  /**
   * Create the Item Writer.  Consumes a vulnerability description, writing it into the database if it does
   * not already exist.
   *
   * @return
   */
  @Bean
  @StepScope
  public JpaItemWriter<VulnerabilityEntity> jpaItemWriter() {
    JpaItemWriter<VulnerabilityEntity> writer = new NistItemWriter(
        jmsTemplate);
    writer.setEntityManagerFactory(entityManagerFactory);
    return writer;
  }

}

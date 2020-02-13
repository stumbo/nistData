package edu.rit.nerParser;

import com.fasterxml.jackson.databind.ObjectMapper;
import edu.rit.nerParser.cve.CVEItem;
import lombok.extern.log4j.Log4j2;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.mapping.DefaultLineMapper;
import org.springframework.batch.item.file.transform.DelimitedLineTokenizer;
import org.springframework.batch.item.json.JacksonJsonObjectReader;
import org.springframework.batch.item.json.JsonItemReader;
import org.springframework.batch.item.json.builder.JsonItemReaderBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

import java.util.LinkedHashMap;
import java.util.Map;

@Log4j2
@Configuration
@EnableBatchProcessing
public class BatchProcessorConfig {
  private final JobBuilderFactory jobBuilderFactory;
  private final StepBuilderFactory stepBuilderFactory;

  BatchProcessorConfig(JobBuilderFactory jobBuilderFactory,
                       StepBuilderFactory stepBuilderFactory) {
    this.jobBuilderFactory = jobBuilderFactory;
    this.stepBuilderFactory = stepBuilderFactory;
  }

  @Bean
  public Job processNistData() {
    return jobBuilderFactory.get("processNistData")
        .incrementer(new RunIdIncrementer())
        .start(processingSteps()).build();
  }

  @Bean
  public Step processingSteps() {
    return stepBuilderFactory.get("processingSteps")
        .<Map<String, String>, Map<String, String>>chunk(10)
        .reader(reader(null))
        .writer(writer())
        .build();
  }

  @Bean
  @StepScope
  public FlatFileItemReader<Map<String, String>> reader(@Value("#{jobParameters['file']}") String file) {
    FlatFileItemReader<Map<String, String>> reader = new FlatFileItemReader<>();
    reader.setResource(new ClassPathResource(file));
    reader.setStrict(false);

    DefaultLineMapper<Map<String, String>> lineMapper = new DefaultLineMapper<>();
    DelimitedLineTokenizer tokenizer = new DelimitedLineTokenizer(":");
    tokenizer.setNames("key", "value");

    lineMapper.setFieldSetMapper((fieldSet) -> {
      Map<String, String> map = new LinkedHashMap<>();
      map.put(fieldSet.readString("key"), fieldSet.readString("value"));
      return map;
    });
    lineMapper.setLineTokenizer(tokenizer);
    reader.setLineMapper(lineMapper);

    return reader;
  }

  /**
   * Item based JSON processing.  Still need to tweek it for NIST vulnerability files.
   * @return
   */
  @Bean
  public JsonItemReader<CVEItem> jsonItemReader() {

    ObjectMapper objectMapper = new ObjectMapper();
    // configure the objectMapper as required
    JacksonJsonObjectReader<CVEItem> jsonObjectReader =
        new JacksonJsonObjectReader<>(CVEItem.class);
    jsonObjectReader.setMapper(objectMapper);

    return new JsonItemReaderBuilder<CVEItem>()
        .jsonObjectReader(jsonObjectReader)
        .resource(new ClassPathResource("trades.json"))
        .name("cveItemReader")
        .build();
  }

  @Bean
  public ItemWriter<Map<String, String>> writer() {
    return (items) -> items.forEach(item -> {
      item.entrySet().forEach(entry -> {
        log.info("key->[{}] Value ->[{}]", entry.getKey(), entry.getValue());
      });
    });
  }

}


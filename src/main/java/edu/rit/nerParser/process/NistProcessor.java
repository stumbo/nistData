package edu.rit.nerParser.process;


import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Component;

import javax.jms.Destination;
import javax.jms.ObjectMessage;
import java.io.File;
import java.util.Collections;
import java.util.concurrent.TimeUnit;

@Log4j2
@Component
public class NistProcessor {
  private final NistData nistData;
  private final Destination nistFileProcessorDestination;
  private final JmsTemplate jmsTemplate;
  private final CsvWriter csvWriter;

  @Value("${queue.name}")
  private String nerQueue;

  @Value("${nist.file.processor}")
  private String nistFileProcessorQueue;

  @Autowired
  public NistProcessor(final NistData nistData,
                       final Destination nistFileProcessorDestination,
                       final JmsTemplate jmsTemplate,
                       final CsvWriter csvWriter) {
    this.nistData = nistData;
    this.nistFileProcessorDestination = nistFileProcessorDestination;
    this.jmsTemplate = jmsTemplate;
    this.csvWriter = csvWriter;
  }

  private Integer getMessageCount(String queueName) {
    Integer totalPendingMessageCount = jmsTemplate.browse(queueName,
        (s, qb) -> Collections.list(qb.getEnumeration()).size());

    return totalPendingMessageCount == null ? 0 : totalPendingMessageCount;
  }

  private Integer totalMessageCount() {
    return log.traceExit(getMessageCount(nerQueue) + getMessageCount(nistFileProcessorQueue));
  }



  public boolean run(String outputFileName) {
    log.info("Loading NIST data");
    nistData.load();

    log.info("Start File Processing");
    for (File file : nistData.getJsonFiles()) {
      jmsTemplate.send(nistFileProcessorDestination, session -> {
        ObjectMessage objectMessage = session.createObjectMessage();
        objectMessage.setObject(file);
        return  objectMessage;
      });
    }

    // Wait for message processing to complete
    while ( totalMessageCount() > 0) {
      log.info("Number of messages to left process: {}", this::totalMessageCount);
      try {
        TimeUnit.SECONDS.sleep(30);
      } catch (InterruptedException e) {
        // do nothing, just continue
      }
    }
    // Write out Vulnerabilities with NLP information
    csvWriter.createCSV(outputFileName);
    return true;
  }
}

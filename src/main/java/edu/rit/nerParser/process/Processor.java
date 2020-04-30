package edu.rit.nerParser.process;


import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Component;

import javax.jms.Destination;
import javax.jms.ObjectMessage;
import java.io.File;
import java.util.Collections;

@Log4j2
@Component
public class Processor {
  private final NistData nistData;
  private final Destination nistFileProcessorDestination;
  private final JmsTemplate jmsTemplate;
  private final CsvWriter csvWriter;

  @Autowired
  public Processor(final NistData nistData,
                   final Destination nistFileProcessorDestination,
                   final JmsTemplate jmsTemplate,
                   final CsvWriter csvWriter) {
    this.nistData = nistData;
    this.nistFileProcessorDestination = nistFileProcessorDestination;
    this.jmsTemplate = jmsTemplate;
    this.csvWriter = csvWriter;
  }

  private Integer getMessageCount() {
      return jmsTemplate.browse((s, qb) -> Collections.list(qb.getEnumeration()).size());
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
    while (getMessageCount() > 0) {
      log.info("Number of messages to left process: {}", this::getMessageCount);
      try {
        Thread.sleep(30000);
      } catch (InterruptedException e) {
        // do nothing, just continue
      }
    }
    // Write out Vulnerabilities with NLP information
    csvWriter.createCSV(outputFileName);
    return true;
  }
}

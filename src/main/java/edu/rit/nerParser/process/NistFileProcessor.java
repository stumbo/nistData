package edu.rit.nerParser.process;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import edu.rit.nerParser.cve.CVEItemService;
import edu.rit.nerParser.cve.data.CVEItem;
import edu.rit.nerParser.cve.data.CveCollection;
import edu.rit.nerParser.data.VulnerabilityEntity;
import edu.rit.nerParser.data.repository.DescriptionRepository;
import edu.rit.nerParser.data.repository.VulnerabilityRepository;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Component;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.ObjectMessage;
import java.io.File;
import java.io.IOException;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

@Component
@Log4j2
public class NistFileProcessor {
  private final ObjectMapper objectMapper;
  private final VulnerabilityRepository vulnerabilityRepository;
  private final DescriptionRepository descriptionRepository;
  private final CVEItemService cveItemService;
  private final JmsTemplate jmsTemplate;

  @Autowired
  NistFileProcessor(final VulnerabilityRepository vulnerabilityRepository,
                    final DescriptionRepository descriptionRepository,
                    final CVEItemService cveItemService,
                    final JmsTemplate jmsTemplate) {
    objectMapper = new ObjectMapper();
    objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    this.vulnerabilityRepository = vulnerabilityRepository;
    this.descriptionRepository = descriptionRepository;
    this.cveItemService = cveItemService;
    this.jmsTemplate = jmsTemplate;
  }

  void processItem(final CVEItem cveItem) {
    Optional<VulnerabilityEntity> oVEntity = cveItemService.process(cveItem);
    oVEntity.ifPresent(entity -> {
      descriptionRepository.findFirstByHash(entity.getDescription().getHash()).ifPresentOrElse(
          entity::setDescription,
          () ->  descriptionRepository.save(entity.getDescription())
      );
      vulnerabilityRepository.save(entity);
      jmsTemplate.send(session -> session.createTextMessage(entity.getDescription().getText()));
    });

  }

  @JmsListener(destination = "{$nist.file.processor}", containerFactory = "nistFileProcessorListener")
  public void receiveMessage(Message message) {
    log.traceEntry();

    if (message instanceof ObjectMessage) {
      ObjectMessage objectMessage = (ObjectMessage) message;
      try {
        Object object = objectMessage.getObject();
        if (object instanceof File) {
          File nistFile = (File) object;

          try {
            CveCollection cveCollection = objectMapper.readValue(nistFile, CveCollection.class);
            for(CVEItem cveItem :cveCollection.cVEItems()) {
              processItem(cveItem);
            }

          } catch(IOException e) {
            log.error("Failure to parse JSON file {}.", nistFile.getName());
          }
        }
      } catch (JMSException e) {
        log.error("Unable to deserialize file object");
        log.catching(e);
      }
    }

    log.traceExit();
  }
}
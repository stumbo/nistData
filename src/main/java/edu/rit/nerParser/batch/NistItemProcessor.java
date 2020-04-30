package edu.rit.nerParser.batch;

import com.sun.istack.NotNull;
import edu.rit.nerParser.cve.CVEItemService;
import edu.rit.nerParser.cve.data.CVEItem;
import edu.rit.nerParser.cve.data.DescriptionDatum;
import edu.rit.nerParser.data.DescriptionEntity;
import edu.rit.nerParser.data.VulnerabilityEntity;
import edu.rit.nerParser.data.repository.DescriptionRepository;
import edu.rit.nerParser.data.repository.VulnerabilityRepository;
import org.apache.commons.lang3.StringUtils;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.jms.core.JmsTemplate;

import javax.jms.Destination;
import javax.jms.ObjectMessage;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

public class NistItemProcessor implements ItemProcessor<CVEItem, VulnerabilityEntity> {

  private final Destination writerDestination;
  private final JmsTemplate jmsTemplate;
  private final CVEItemService cveItemService;

  public NistItemProcessor(final Destination writerDestination,
                           final JmsTemplate jmsTemplate,
                           final CVEItemService cveItemService) {
    this.writerDestination = writerDestination;
    this.jmsTemplate = jmsTemplate;
    this.cveItemService = cveItemService;
  }

  @Override
  public VulnerabilityEntity process(@NotNull final CVEItem cveItem) throws Exception {
    cveItemService.process(cveItem).ifPresent(entity -> {
      jmsTemplate.send(writerDestination, session -> {
        ObjectMessage objectMessage = session.createObjectMessage();
        objectMessage.setObject(entity);
        return objectMessage;
      });
    });
      return null;
  }
}

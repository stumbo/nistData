package edu.rit.nerParser.batch;

import edu.rit.nerParser.data.DescriptionEntity;
import edu.rit.nerParser.data.VulnerabilityEntity;
import edu.rit.nerParser.data.repository.DescriptionRepository;
import lombok.extern.log4j.Log4j2;
import org.springframework.batch.item.database.JpaItemWriter;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Log4j2
public class NistItemWriter extends JpaItemWriter<VulnerabilityEntity> {
  private final DescriptionRepository descriptionRepository;
  private final JmsTemplate jmsTemplate;

  NistItemWriter(final DescriptionRepository descriptionRepository,
                 final JmsTemplate jmsTemplate) {
    super();
    this.descriptionRepository = descriptionRepository;
    this.jmsTemplate = jmsTemplate;
  }

  @Override
  @Transactional
  public void write(List<? extends VulnerabilityEntity> items) {
    for (VulnerabilityEntity item: items) {
      Optional<DescriptionEntity> entity = descriptionRepository.findFirstByHash(item.getDescription().getHash());
      entity.ifPresent(item::setDescription);
    }

//    log.info("items length: {} item 0 name: {} . text: {}", items.size(), items.get(0).getName(), items.get(0).getDescription().getText());
    super.write(items);

    // Send a message to the NER processor with each message
      items.forEach(m -> jmsTemplate.send(
          session -> session.createTextMessage(m.getDescription().getText())));
  }

}

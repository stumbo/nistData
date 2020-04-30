package edu.rit.nerParser.process;

import edu.rit.nerParser.data.DescriptionEntity;
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

@Log4j2
@Component
public class nistWriter {
  private final DescriptionRepository descriptionRepository;
  private final VulnerabilityRepository vulnerabilityRepository;
  private final JmsTemplate jmsTemplate;

  @Autowired
  public nistWriter(final VulnerabilityRepository vulnerabilityRepository,
                    final DescriptionRepository descriptionRepository,
                    final JmsTemplate jmsTemplate) {
    this.vulnerabilityRepository = vulnerabilityRepository;
    this.descriptionRepository = descriptionRepository;
    this.jmsTemplate = jmsTemplate;
  }

  @JmsListener(destination = "${nist.write.queue}", containerFactory = "nistWriterListenerFactory")
  public void receiveMessage(Message message) {
    log.traceEntry();
    if (message instanceof ObjectMessage) {
      ObjectMessage objectMessage = (ObjectMessage) message;
      try {
        Object object = objectMessage.getObject();
        if (object instanceof VulnerabilityEntity) {
          VulnerabilityEntity entity = (VulnerabilityEntity) object;
          descriptionRepository.findFirstByHash(entity.getDescription().getHash()).ifPresentOrElse(
              entity::setDescription,
              () ->  descriptionRepository.save(entity.getDescription())
          );
          vulnerabilityRepository.save(entity);

          jmsTemplate.send(session -> session.createTextMessage(entity.getDescription().getText()));
        }

      } catch (JMSException e) {
        log.error("Unable to deserialize vulnerability object");
        log.catching(e);
      }
    }
    log.traceExit();
  }
}

package edu.rit.nerParser.queue;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.command.ActiveMQQueue;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jms.config.DefaultJmsListenerContainerFactory;
import org.springframework.jms.connection.CachingConnectionFactory;
import org.springframework.jms.core.JmsTemplate;

import javax.jms.Destination;

/**
 * JMS Message Queue Configuration
 */
@Configuration
public class QueueConfig {
  @Value("${spring.activemq.broker-url}")
  private String brokerUrl;

  @Value("${queue.name}")
  private String nerDestination;

  @Value("nistFileProcessorListener")
  private String nistFileProcessor;

  @Bean
  public
  ActiveMQConnectionFactory activeMQConnectionFactory() {
    ActiveMQConnectionFactory activeMQConnectionFactory = new ActiveMQConnectionFactory();
    activeMQConnectionFactory.setTrustAllPackages(true);
    activeMQConnectionFactory.setBrokerURL(brokerUrl);

    return activeMQConnectionFactory;
  }

  @Bean(name = "nerListenerFactory")
  public DefaultJmsListenerContainerFactory nerListenerFactory() {
    DefaultJmsListenerContainerFactory factory = new DefaultJmsListenerContainerFactory();
    factory.setConnectionFactory(activeMQConnectionFactory());
    factory.setConcurrency("1-5");
    return factory;
  }

  @Bean(name = "nistFileProcessorListener")
  public DefaultJmsListenerContainerFactory nistFileProcessorListener() {
    DefaultJmsListenerContainerFactory factory = new DefaultJmsListenerContainerFactory();
    factory.setConnectionFactory(activeMQConnectionFactory());
    factory.setConcurrency("1-5");
    return  factory;
  }

  @Bean
  public CachingConnectionFactory cachingConnectionFactory() {
    return new CachingConnectionFactory(activeMQConnectionFactory());
  }

  @Bean
  public Destination nerDestination() {
    return new ActiveMQQueue(nerDestination);
  }

  @Bean
  public Destination nistFileProcessorDestination() {
    return new ActiveMQQueue(nistFileProcessor);
  }

  @Bean
  public JmsTemplate jmsTemplate() {
    JmsTemplate jmsTemplate = new JmsTemplate(cachingConnectionFactory());
    jmsTemplate.setDefaultDestination(nerDestination());
    return jmsTemplate;
  }

}

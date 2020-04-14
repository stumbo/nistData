package edu.rit.nerParser;

import edu.rit.nerParser.data.DescriptionEntity;
import edu.rit.nerParser.data.repository.DescriptionRepository;
import edu.stanford.nlp.pipeline.CoreDocument;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.Properties;
import java.util.stream.Collectors;

/**
 * Name Entity Recognition using the Stanford Natural Language Processor
 *
 * @author wstumbo
 */
@Log4j2
@Component
public class NLP {
  private final StanfordCoreNLP pipeline;
  private final DescriptionRepository descriptionRepository;

  @Autowired
  NLP(final DescriptionRepository descriptionRepository) {
    Properties nlpProperties = new Properties();
    nlpProperties.setProperty("annotators", "tokenize,ssplit,pos,lemma,ner");
    pipeline = new StanfordCoreNLP(nlpProperties);
    this.descriptionRepository = descriptionRepository;
  }

  /**
   * Perform Name Entity Recognition on a string
   *
   * @param text  Text to analyze
   */
  @JmsListener(destination = "${queue.name}")
  public void receiveMessage(String text) {
    log.traceEntry();
    if (text == null) {
      return;
    }
    Optional<DescriptionEntity> entity = descriptionRepository.findFirstByHash(text.hashCode());
    if (entity.isPresent()) {
      CoreDocument doc = new CoreDocument(text);
      pipeline.annotate(doc);
      String nerValue = doc.tokens().stream()
          .map(token -> "(" + token.word() + "," + token.ner() + ")")
          .collect(Collectors.joining(" "));
      entity.get().setNer(nerValue);
      descriptionRepository.save(entity.get());
      log.traceExit("NER Result: {}", nerValue);
    }

  }

}

package edu.rit.nerParser.process;

import edu.rit.nerParser.data.DescriptionEntity;
import edu.rit.nerParser.data.repository.DescriptionRepository;
import edu.stanford.nlp.pipeline.CoreDocument;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
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
   * @param text Text to analyze
   */
  @JmsListener(destination = "${queue.name}", containerFactory = "nerListenerFactory")
  public void receiveMessage(String text) {
    if (text == null) {
      return;
    }
    Optional<DescriptionEntity> entity = descriptionRepository.findFirstByHash(text.hashCode());
    entity.ifPresent(descriptionEntity -> {
          if (StringUtils.isEmpty(descriptionEntity.getNer())) {
            CoreDocument doc = new CoreDocument(text);
            pipeline.annotate(doc);
            String nerValue = doc.tokens().stream()
                .map(token -> "(" + token.word() + "," + token.ner() + ")")
                .collect(Collectors.joining(" "));
            descriptionEntity.setNer(nerValue);
              descriptionRepository.save(descriptionEntity);
          }
        }
    );

  }

}


package edu.rit.nerParser;

import edu.stanford.nlp.pipeline.CoreDocument;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import lombok.extern.log4j.Log4j2;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

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
 // private final StanfordCoreNLP pipeline;

  @Autowired
  NLP() {
    Properties nlpProperties = new Properties();
//    nlpProperties.setProperty("annotators", "tokenize,ssplit,pos,lemma,ner");
 //   pipeline = new StanfordCoreNLP(nlpProperties);
  }

  /**
   * Perform Name Entity Recognition on a string
   *
   * @param text  Text to analyze
   * @return NER output as a string
   */
  public String doNER(String text) {
    if (text == null) {
      return StringUtils.EMPTY;
    }
    CoreDocument doc = new CoreDocument(text);
//    pipeline.annotate(doc);
    return (
        doc.tokens().stream()
            .map(token -> "(" + token.word() + "," + token.ner() + ")")
            .collect(Collectors.joining(" "))
    );
  }

}

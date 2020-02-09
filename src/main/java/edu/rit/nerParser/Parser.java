package edu.rit.nerParser;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import edu.rit.nerParser.cve.CVEItem;
import edu.rit.nerParser.cve.CveCollection;
import edu.rit.nerParser.cve.DescriptionDatum;
import edu.rit.nerParser.data.DescriptionEntity;
import edu.rit.nerParser.data.VulnerabilityEntity;
import edu.rit.nerParser.data.repository.DescriptionRepository;
import edu.rit.nerParser.data.repository.VulnerabilityRepository;
import lombok.extern.log4j.Log4j2;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.File;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Log4j2
@Component
public class Parser {

  private final VulnerabilityRepository vRepo;
  private final DescriptionRepository descRepo;
  private final NLP nlp;

  private static final DateTimeFormatter VULNERABILITY_PUBLISHED_DATE = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mmX");

  @Autowired
  Parser(VulnerabilityRepository vRepo,
         DescriptionRepository descRepo,
         NLP nlp) {
    this.vRepo = vRepo;
    this.descRepo = descRepo;
    this.nlp = nlp;
  }

  public void processNISTFiles(File[] fileArray) {

      for (File dataFile : fileArray) {
        System.out.println("Processing: " + dataFile.getName());
        processFile(dataFile);
      }
  }

  public void doNER() {
    descRepo.getAllByNerIsNull().forEach(d -> {
      d.setNer(nlp.doNER(d.getText()));
      descRepo.save(d);
    });
  }

  /**
   * Process a JSON data file building a set of vulnerability identifiers
   * and associated descriptions.
   *
   * @param dataFile file to process
   * @return Map of Vulnerability identifiers to descriptions
   */
  private Map<String, String> processFile(File dataFile) {
    Map<String, String> vulnerabilities = new HashMap<>();
    ObjectMapper objectMapper = new ObjectMapper();
    objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    try {
      CveCollection cveCollection = objectMapper.readValue(dataFile, CveCollection.class);

      for (CVEItem cveItem : cveCollection.cVEItems()) {
        Instant publishedDate = Instant.from(VULNERABILITY_PUBLISHED_DATE.parse(cveItem.publishedDate()));
            Optional<VulnerabilityEntity> vEntity = vRepo.getFirstByName(cveItem.cve().cVEDataMeta().id());
        if (vEntity.isEmpty() || (vEntity.get().getUpdateTime().compareTo(publishedDate) < 0)) {
          VulnerabilityEntity vulnerability = new VulnerabilityEntity();
          vulnerability.setName(cveItem.cve().cVEDataMeta().id());
          vulnerability.setUpdateTime(publishedDate);
          String tVal = cveItem.cve().description().descriptionData().stream()
              .filter(datum -> datum.lang().equals("en"))
              .map(DescriptionDatum::value)
              .findFirst().orElse(StringUtils.EMPTY) ;

          Optional<DescriptionEntity> descriptionEntity = descRepo.findFirstByHash(tVal.hashCode());
          if (descriptionEntity.isEmpty()) {
            DescriptionEntity description = new DescriptionEntity();
            description.setHash(tVal.hashCode());
            description.setText(tVal);

            descRepo.save(description);

            vulnerability.setDescription(description);

            log.info("Hash: {}  Text: {} ", description.getHash(), description.getText());
          } else {
            vulnerability.setDescription(descriptionEntity.get());
            log.info("Hash: {}  Text: {} ", descriptionEntity.get().getHash(), descriptionEntity.get().getText());
          }

          vRepo.save(vulnerability);

        }

        if (!vulnerabilities.containsKey(cveItem.cve().cVEDataMeta().id())) {
          String dataItem = cveItem.cve().description().descriptionData().stream()
              .filter(datum -> datum.lang().equals("en"))
              .map(DescriptionDatum::value)
              .findFirst()
              .orElse(StringUtils.EMPTY);

          vulnerabilities.put(cveItem.cve().cVEDataMeta().id(), dataItem);
        } else {
          log.info("Duplicate Key Found: " + cveItem.cve().cVEDataMeta().id() + ". Value ignored.");
        }
      }

    } catch (Exception e) {
      log.error("File Processing Exception", e);
      throw new RuntimeException("Processing failed during file processing.");
    }

    return vulnerabilities;
  }
}

package edu.rit.nerParser.batch;

import edu.rit.nerParser.cve.CVEItem;
import edu.rit.nerParser.cve.DescriptionDatum;
import edu.rit.nerParser.data.DescriptionEntity;
import edu.rit.nerParser.data.VulnerabilityEntity;
import edu.rit.nerParser.data.repository.DescriptionRepository;
import edu.rit.nerParser.data.repository.VulnerabilityRepository;
import org.apache.commons.lang3.StringUtils;
import org.springframework.batch.item.ItemProcessor;

import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

public class NistItemProcessor implements ItemProcessor<CVEItem, VulnerabilityEntity> {

  private final DescriptionRepository descriptionRepository;
  private final VulnerabilityRepository vulnerabilityRepository;
  private static final DateTimeFormatter VULNERABILITY_PUBLISHED_DATE = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mmX");

  public NistItemProcessor(final DescriptionRepository descriptionRepository,
                           final VulnerabilityRepository vulnerabilityRepository) {
    this.descriptionRepository = descriptionRepository;
    this.vulnerabilityRepository = vulnerabilityRepository;
  }
  private DescriptionEntity buildDescription(String text) {
    DescriptionEntity entity = new DescriptionEntity();
    entity.setText(text);
    entity.setHash(text.hashCode());

    return entity;
  }

  @Override
  public VulnerabilityEntity process(CVEItem cveItem) throws Exception {
    Instant publishedDate = Instant.from(VULNERABILITY_PUBLISHED_DATE.parse(cveItem.publishedDate()));
    String name = cveItem.cve().cVEDataMeta().id();

    if (vulnerabilityRepository.getFirstByName(name).isEmpty()) {
      VulnerabilityEntity entity = new VulnerabilityEntity();
      entity.setName(name);
      entity.setUpdateTime(publishedDate);
      String tVal = cveItem.cve().description().descriptionData().stream()
          .filter(datum -> datum.lang().equals("en"))
          .map(DescriptionDatum::value)
          .findFirst().orElse(StringUtils.EMPTY);

      Optional<DescriptionEntity> description = descriptionRepository.findFirstByHash(tVal.hashCode());
      description.ifPresent(entity::setDescription);
      description.ifPresentOrElse(entity::setDescription,
          () -> entity.setDescription(buildDescription(tVal)));

      return entity;
    } else {
      return null;
    }
  }
}

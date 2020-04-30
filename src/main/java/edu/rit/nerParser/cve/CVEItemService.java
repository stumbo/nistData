package edu.rit.nerParser.cve;

import edu.rit.nerParser.cve.data.CVEItem;
import edu.rit.nerParser.cve.data.DescriptionDatum;
import edu.rit.nerParser.data.DescriptionEntity;
import edu.rit.nerParser.data.VulnerabilityEntity;
import edu.rit.nerParser.data.repository.DescriptionRepository;
import edu.rit.nerParser.data.repository.VulnerabilityRepository;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

@Component
@Log4j2
public class CVEItemService {
  private final DescriptionRepository descriptionRepository;
  private final VulnerabilityRepository vulnerabilityRepository;

  private static final DateTimeFormatter VULNERABILITY_PUBLISHED_DATE = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mmX");



  @Autowired
  CVEItemService(final DescriptionRepository descriptionRepository,
                 final VulnerabilityRepository vulnerabilityRepository){
    this.descriptionRepository = descriptionRepository;
    this.vulnerabilityRepository = vulnerabilityRepository;
  }

  private DescriptionEntity buildDescription(String text) {
    DescriptionEntity entity = new DescriptionEntity();
    entity.setText(text);
    entity.setHash(text.hashCode());

    return entity;
  }

  /**
   * Extract needed values from a CVEItem and build the vulnerability entity and its
   * associated description entity.
   *
   * @param cveItem to process
   * @return create vulnerability entity
   */
  public Optional<VulnerabilityEntity> process(CVEItem cveItem) {
    Instant publishedDate = Instant.from(VULNERABILITY_PUBLISHED_DATE.parse(cveItem.publishedDate()));
    String name = cveItem.cve().cVEDataMeta().id();

    VulnerabilityEntity entity = vulnerabilityRepository.getFirstByName(name).orElse(new VulnerabilityEntity());

      if (StringUtils.isEmpty(entity.getName())) {
        entity.setName(name);
      }

    // If the date is newer, we need to replace the time stamp and
    // the description text.
      if (entity.getUpdateTime() == null || entity.getUpdateTime().isBefore(publishedDate)) {
        entity.setUpdateTime(publishedDate);
        String tVal = cveItem.cve().description().descriptionData().stream()
            .filter(datum -> datum.lang().equals("en"))
            .map(DescriptionDatum::value)
            .findFirst().orElse(StringUtils.EMPTY);

        Optional<DescriptionEntity> description = descriptionRepository.findFirstByHash(tVal.hashCode());
        description.ifPresent(entity::setDescription);
        description.ifPresentOrElse(entity::setDescription,
            () -> entity.setDescription(buildDescription(tVal)));

        return Optional.of(entity);
      }
    return Optional.empty();
  }


}

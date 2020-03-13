package edu.rit.nerParser.batch;

import edu.rit.nerParser.cve.CVEItem;
import edu.rit.nerParser.cve.DescriptionDatum;
import edu.rit.nerParser.data.DescriptionEntity;
import edu.rit.nerParser.data.VulnerabilityEntity;
import org.apache.commons.lang3.StringUtils;
import org.springframework.batch.item.ItemProcessor;

import java.time.Instant;
import java.time.format.DateTimeFormatter;

public class NistItemProcessor implements ItemProcessor<CVEItem, VulnerabilityEntity> {

  private static final DateTimeFormatter VULNERABILITY_PUBLISHED_DATE = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mmX");

  @Override
  public VulnerabilityEntity process(CVEItem cveItem) throws Exception {
    Instant publishedDate = Instant.from(VULNERABILITY_PUBLISHED_DATE.parse(cveItem.publishedDate()));
    VulnerabilityEntity entity = new VulnerabilityEntity();
    entity.setName(cveItem.cve().cVEDataMeta().id());
    entity.setUpdateTime(publishedDate);
    String tVal = cveItem.cve().description().descriptionData().stream()
        .filter(datum -> datum.lang().equals("en"))
        .map(DescriptionDatum::value)
        .findFirst().orElse(StringUtils.EMPTY);

    DescriptionEntity description = new DescriptionEntity();
    description.setHash(tVal.hashCode());
    description.setText(tVal);

    entity.setDescription(description);
    return entity;
  }
}

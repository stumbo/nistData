package edu.rit.nerParser.batch;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import edu.rit.nerParser.cve.CVEItem;
import edu.rit.nerParser.cve.CveCollection;
import lombok.extern.log4j.Log4j2;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.NonTransientResourceException;
import org.springframework.batch.item.ParseException;
import org.springframework.batch.item.UnexpectedInputException;

import java.io.File;
import java.io.IOException;
import java.util.List;

@Log4j2
public class NistItemReader implements ItemReader<CVEItem> {
  private final File dataFile;
  private CveCollection cveCollection = null;
  private List<CVEItem> cveItems;
  private int cveItemCount;

  NistItemReader(File dataFile) {
    super();
    this.dataFile = dataFile;
  }

  private void setup() throws JsonProcessingException, IOException {
    ObjectMapper objectMapper = new ObjectMapper();
    objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    cveCollection = objectMapper.readValue(dataFile, CveCollection.class);
    cveItems = cveCollection.cVEItems();
  }

  @Override
  public CVEItem read() throws Exception, UnexpectedInputException, ParseException, NonTransientResourceException {
    if (cveCollection == null) {
      setup();
    }
    if (cveItemCount < cveItems.size()) {
      CVEItem item = cveItems.get(cveItemCount);
      cveItemCount = cveItemCount + 1;
      return item;
    }
    log.info("{} completing and returning null", dataFile.getName());
    return null;
  }

}

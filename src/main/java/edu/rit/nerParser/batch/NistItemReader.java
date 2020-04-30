package edu.rit.nerParser.batch;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import edu.rit.nerParser.cve.data.CVEItem;
import edu.rit.nerParser.cve.data.CveCollection;
import lombok.extern.log4j.Log4j2;
import org.springframework.batch.item.ItemReader;

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

  private void setup() throws IOException {
    ObjectMapper objectMapper = new ObjectMapper();
    objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    cveCollection = objectMapper.readValue(dataFile, CveCollection.class);
    cveItems = cveCollection.cVEItems();
  }

  @Override
  public CVEItem read() throws Exception {
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

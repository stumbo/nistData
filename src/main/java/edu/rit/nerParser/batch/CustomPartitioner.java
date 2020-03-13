package edu.rit.nerParser.batch;

import lombok.extern.log4j.Log4j2;
import org.springframework.batch.core.partition.support.Partitioner;
import org.springframework.batch.item.ExecutionContext;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 * Custom partitioner
 *
 * Assign each input process ExecutionContext a datafile.
 */
@Log4j2
public class CustomPartitioner implements Partitioner {
  private final File[] dataFiles;

  CustomPartitioner(File[] dataFiles) {
    super();
    this.dataFiles = dataFiles;
  }

  @Override
  public Map<String, ExecutionContext> partition(int gridSize) {
    Map<String, ExecutionContext> result = new HashMap<String, ExecutionContext>();

    log.info("Grid Size: {}", gridSize);

    for (int i = 1; i <= gridSize; i++) {
      ExecutionContext value = new ExecutionContext();

      value.put("dataFile", dataFiles[i-1]);

      result.put("partition" + i, value);

      log.info("Partition {} : datafile :{}", i, dataFiles[i-1].getName());
    }

    return result;
  }
}

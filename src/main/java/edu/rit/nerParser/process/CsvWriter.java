package edu.rit.nerParser.process;

import edu.rit.nerParser.data.VulnerabilityEntity;
import edu.rit.nerParser.data.repository.VulnerabilityRepository;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

/**
 * CsvWriter
 *
 * Convenience functions for writing a CSV file
 *
 * @author wstumbo
 */
@Log4j2
@Component
public class CsvWriter {
  private final VulnerabilityRepository vRepository;

  @Autowired
    CsvWriter(VulnerabilityRepository vRepository) {
      this.vRepository = vRepository;
    }

    /**
     * Write CSV File headers
     *
     * @param csvFile Buffered File to write to
     * @throws IOException
     */
    private static void addHeaders(BufferedWriter csvFile) throws IOException {
        csvFile.append("CVE-ID");
        csvFile.append(",");
        csvFile.append("Annotated Description");
        csvFile.append("\n");
    }

    /**
     * Write a line of data to the CSV Buffered File
     *
     * @param csvFile Buffered File to write to
     * @param cveID Vulnerability identifier
     * @param description Output of Name Entity Recognition
     * @throws IOException
     */
    private static void writeLine(BufferedWriter csvFile, String cveID, String description) throws IOException {
      if (cveID == null || description == null) {
        log.error("Attempt to write empty fields cveID: {}  : descrtiption {}.", cveID, description);
      } else {
        csvFile.append(cveID);
        csvFile.append(",");
        String formattedDescription = "\"" + description.replace("\"", "\"\"") + "\"";
        csvFile.append(formattedDescription);
        csvFile.append("\n");
        csvFile.flush();
      }
    }

    public void createCSV(String outputFile) {
      try (BufferedWriter writer = new BufferedWriter(new FileWriter(outputFile, false))) {
        CsvWriter.addHeaders(writer);

        for(VulnerabilityEntity entity : vRepository.findAllByOrderByName()) {
          CsvWriter.writeLine(writer, entity.getName(), entity.getDescription().getNer());
        }

      } catch (IOException e) {
        log.error("File IO Exception raised, aborting processing", e);
        throw new RuntimeException("Processing Failure");
      }
    }

}
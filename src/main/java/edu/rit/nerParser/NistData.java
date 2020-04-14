package edu.rit.nerParser;

import org.springframework.stereotype.Component;
import us.springett.nistdatamirror.NistDataMirror;

import java.io.File;
import java.io.FilenameFilter;

/**
 * NistData
 *
 * Handle all interactions with the NIST Vulnerability data loads.
 *  - update the files
 *  - retrieve files for processing
 *
 * @author wstumbo
 *
 */

@Component
public class NistData {
  private static final String DATA_DIRECTORY = "./data";

  /**
   * Use NistDataMirror to download and maintain a copy of the NIST
   * vulnerability files
   *
   * @return true if successful
   */
  public boolean load() {
    // Redirect System.Out and System.Err, package assumes output is directed to console.
    //PrintStream originalOutput = System.out;
    //PrintStream originalErr = System.err;

    //ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
   // PrintStream tempOutput = new PrintStream(outputStream);
    //System.setOut(tempOutput);

    // replace with a log file...
   // ByteArrayOutputStream errStream = new ByteArrayOutputStream();
   // PrintStream tempErr = new PrintStream(errStream);
   // System.setErr(tempErr);

    NistDataMirror dataMirror = new NistDataMirror(DATA_DIRECTORY);

    // NIST supports to JSON versions, select the newer version.
    dataMirror.mirror("1.1");

    //System.out.flush();
   // System.err.flush();

    //System.setOut(originalOutput);
    //System.setErr(originalErr);

   // String outString = new String(outputStream.toByteArray(), StandardCharsets.UTF_8);
   // String errString = new String(errStream.toByteArray(), StandardCharsets.UTF_8);

    // Validate download succeeded
   // return !outString.contains("failed") && !errString.contains("Error");
    return true;
  }

  /**
   * Get an array containing all the json vulnerability files
   *
   * @return array of files
   */
  public File[] getJsonFiles() {
    File dataDirectory = new File(DATA_DIRECTORY);

    return dataDirectory.listFiles(jsonFiles);
  }

  /**
   * File filter to just retrieve files that end in .json.
   */
  private static final FilenameFilter jsonFiles = (file, name) -> {
    String lowercaseName = name.toLowerCase();
    // modified.json file contains duplicate entries, no need to process
    if (lowercaseName.endsWith("modified.json")) {
      return false;
    } else return lowercaseName.endsWith(".json");
  };

}

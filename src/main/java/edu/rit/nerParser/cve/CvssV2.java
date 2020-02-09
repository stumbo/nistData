
package edu.rit.nerParser.cve;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Accessors(fluent = true)
@Builder
@SuppressWarnings("unused")
public class CvssV2 {

    private String accessComplexity;
    private String accessVector;
    private String authentication;
    private String availabilityImpact;
    private long baseScore;
    private String confidentialityImpact;
    private String integrityImpact;
    private String vectorString;
    private String version;

}

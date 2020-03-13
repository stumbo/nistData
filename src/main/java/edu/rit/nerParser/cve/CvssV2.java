
package edu.rit.nerParser.cve;

import com.fasterxml.jackson.annotation.JsonProperty;
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

    @JsonProperty("accessComplexity")
    private String accessComplexity;

    @JsonProperty("accessVector")
    private String accessVector;

    @JsonProperty("authentication")
    private String authentication;

    @JsonProperty("availabilityImpact")
    private String availabilityImpact;

    @JsonProperty("baseScore")
    private long baseScore;

    @JsonProperty("confidentialityImpact")
    private String confidentialityImpact;

    @JsonProperty("integrityImpact")
    private String integrityImpact;

    @JsonProperty("vectorString")
    private String vectorString;

    @JsonProperty("version")
    private String version;
}


package edu.rit.nerParser.cve.data;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
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
@JsonIgnoreProperties(ignoreUnknown = true)
public class BaseMetricV2 {

    @JsonProperty("cvssV2")
    private CvssV2 cvssV2;

    @JsonProperty("exploitabilityScore")
    private long exploitabilityScore;

    @JsonProperty("impactScore")
    private double impactScore;

    @JsonProperty("obtainAllPrivilege")
    private Boolean obtainAllPrivilege;

    @JsonProperty("obtainOtherPrivilege")
    private Boolean obtainOtherPrivilege;

    @JsonProperty("obtainUserPrivilege")
    private Boolean obtainUserPrivilege;

    @JsonProperty("severity")
    private String severity;

    @JsonProperty("userInteractionRequired")
    private Boolean userInteractionRequired;

}

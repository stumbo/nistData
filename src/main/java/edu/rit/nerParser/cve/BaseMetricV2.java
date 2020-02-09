
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
public class BaseMetricV2 {

    private CvssV2 cvssV2;
    private long exploitabilityScore;
    private double impactScore;
    private Boolean obtainAllPrivilege;
    private Boolean obtainOtherPrivilege;
    private Boolean obtainUserPrivilege;
    private String severity;
    private Boolean userInteractionRequired;

}

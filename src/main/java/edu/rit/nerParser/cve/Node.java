
package edu.rit.nerParser.cve;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.util.List;

@NoArgsConstructor
@Data
@Accessors(fluent = true)
@SuppressWarnings("unused")
@JsonIgnoreProperties(ignoreUnknown = true)
public class Node {

    @JsonProperty("cpe_match")
    private List<CpeMatch> cpeMatch;

    @JsonProperty("operator")
    private String operator;

}

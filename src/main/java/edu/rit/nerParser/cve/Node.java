
package edu.rit.nerParser.cve;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.util.List;

@NoArgsConstructor
@Data
@Accessors(fluent = true)
@SuppressWarnings("unused")
public class Node {

    @JsonProperty("cpe_match")
    private List<CpeMatch> cpeMatch;
    private String operator;

}


package edu.rit.nerParser.cve.data;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Accessors(fluent = true)
@Builder
@SuppressWarnings("unused")
public class ProblemtypeDatum {

    @JsonProperty("description")
    private List<ProblemTypeDescription> description;

}

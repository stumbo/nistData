
package edu.rit.nerParser.cve;

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
public class References {

    @JsonProperty("reference_data")
    private List<ReferenceDatum> referenceData;

}

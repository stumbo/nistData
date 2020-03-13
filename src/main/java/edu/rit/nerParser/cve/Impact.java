
package edu.rit.nerParser.cve;

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
public class Impact {

    @JsonProperty("baseMetricV2")
    private BaseMetricV2 baseMetricV2;
}

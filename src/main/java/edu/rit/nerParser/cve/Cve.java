
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
public class Cve {

    @JsonProperty("CVE_data_meta")
    private CVEDataMeta cVEDataMeta;

    @JsonProperty("data_format")
    private String dataFormat;

    @JsonProperty("data_type")
    private String dataType;

    @JsonProperty("data_version")
    private String dataVersion;

    @JsonProperty("description")
    private Description description;

    @JsonProperty("problemtype")
    private Problemtype problemtype;

    @JsonProperty("references")
    private References references;

}

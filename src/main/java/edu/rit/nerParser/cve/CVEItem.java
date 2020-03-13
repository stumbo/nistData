
package edu.rit.nerParser.cve;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

@NoArgsConstructor
@Data
@Accessors(fluent = true)
@SuppressWarnings("unused")
public class CVEItem {

    @JsonProperty("configurations")
    private Configurations configurations;

    @JsonProperty("cve")
    private Cve cve;

    @JsonProperty("impact")
    private Impact impact;

    @JsonProperty("lastModifiedDate")
    private String lastModifiedDate;

    @JsonProperty("publishedDate")
    private String publishedDate;
}

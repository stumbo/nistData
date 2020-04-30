
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
public class CpeMatch {

    @JsonProperty("cpe23Uri")
    private String cpe23Uri;

    @JsonProperty("vulnerable")
    private Boolean vulnerable;

    @JsonProperty("versionEndIncluding")
    private String versionEndIncluding;

    @JsonProperty("versionEndExcluding")
    private String versionEndExcluding;

}

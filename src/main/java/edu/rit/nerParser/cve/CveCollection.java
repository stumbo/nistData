
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
public class CveCollection {

    @JsonProperty("CVE_data_format")
    private String cVEDataFormat;
    @JsonProperty("CVE_data_numberOfCVEs")
    private String cVEDataNumberOfCVEs;
    @JsonProperty("CVE_data_timestamp")
    private String cVEDataTimestamp;
    @JsonProperty("CVE_data_type")
    private String cVEDataType;
    @JsonProperty("CVE_data_version")
    private String cVEDataVersion;
    @JsonProperty("CVE_Items")
    private List<CVEItem> cVEItems;

}

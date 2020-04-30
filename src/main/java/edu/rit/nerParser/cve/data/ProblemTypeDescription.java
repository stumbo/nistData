package edu.rit.nerParser.cve.data;

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
public class ProblemTypeDescription {
  @JsonProperty("lang")
  private String lang;

  @JsonProperty("value")
  private String value;

}

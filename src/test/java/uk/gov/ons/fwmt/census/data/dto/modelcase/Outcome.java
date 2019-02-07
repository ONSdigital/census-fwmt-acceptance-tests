package uk.gov.ons.fwmt.census.data.dto.modelcase;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
public class Outcome {
  private UUID id = null;
  private Integer result = null;
  private String dateCreated = null;
}


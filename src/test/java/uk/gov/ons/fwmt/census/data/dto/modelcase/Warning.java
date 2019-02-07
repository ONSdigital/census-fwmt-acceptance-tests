package uk.gov.ons.fwmt.census.data.dto.modelcase;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
public class Warning {
  private UUID outcomeId = null;
  private UUID warningId = null;
  private String warningContent = null;
  private String dateCreated = null;

}


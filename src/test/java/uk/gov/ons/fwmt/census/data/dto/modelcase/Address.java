package uk.gov.ons.fwmt.census.data.dto.modelcase;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
public class Address {
  private Long uprn = null;
  private List<String> lines = null;
  private String postCode = null;
}


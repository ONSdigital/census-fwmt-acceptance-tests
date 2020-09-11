package uk.gov.ons.census.fwmt.tests.acceptance.steps.inbound.common;

import lombok.Builder;
import lombok.Data;
import lombok.ToString;

@Builder
@Data
@ToString
public class NodeCheck {
  private String url;
  private String name;
  private boolean isSuccesful;
  private String failureMsg;
}

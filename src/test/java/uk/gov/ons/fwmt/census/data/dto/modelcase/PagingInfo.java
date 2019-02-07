package uk.gov.ons.fwmt.census.data.dto.modelcase;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class PagingInfo {
  private Integer pageNo = null;
  private Integer pageSize = null;
  private Integer pageCount = null;
  private Integer totalCount = null;
  private String previous = null;
  private String next = null;
}


package uk.gov.ons.fwmt.census.data.dto.modelcase;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
public class ModelCase {
  private String id = null;
  private String reference = null;
  private String caseType = null;
  private StateEnum state = null;
  private String category = null;
  private String estabType = null;
  private String coordCode = null;
  private List<Outcome> outcomes = null;
  private List<Warning> warnings = null;
  private List<Note> notes = null;
  private Contact contact = null;
  private Address address = null;
  private LatLong location = null;
  private Integer htc = null;
  private Integer priority = null;
  private String description = null;
  private String specialInstructions = null;
  private String holdUntil = null;
  //  private List<Link> _links = null;

  public enum StateEnum {
    OPEN("open"),

    CLOSED("closed");

    private String value;

    StateEnum(String value) {
      this.value = value;
    }

    @JsonCreator
    public static StateEnum fromValue(String text) {
      for (StateEnum b : StateEnum.values()) {
        if (String.valueOf(b.value).equals(text)) {
          return b;
        }
      }
      return null;
    }

    @Override
    @JsonValue
    public String toString() {
      return String.valueOf(value);
    }
  }
}


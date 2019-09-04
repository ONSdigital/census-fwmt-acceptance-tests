@Census @Acceptance @Outcome @NonValidHousehold
Feature: Non-valid household Outcome Tests

  Scenario Outline: As a Gateway I can receive final Non Valid Household outcome of cases from TM and create Census Events
      Given TM sends a "<InputMessage>" Census Case Outcome to the Gateway
      And the Primary Outcome is "Non-valid household"
      And the Secondary Outcome is "<SecondaryOutcome>"
    When the Outcome Service process non-valid household message
      Then the Outcome Service should create a valid "<CaseEvent>"
      And and of the correct "<EventType>" 
  
    Examples:
      | InputMessage         |  SecondaryOutcome    | CaseEvent   | EventType            |
      | Derelict             | Derelict             | Field.other | ADDRESS_NOT_VALID    |
      | Demolished           | Demolished           | Field.other | ADDRESS_NOT_VALID    |
      | Cant find            | Cant find            | Field.other | ADDRESS_NOT_VALID    |
      | Unaddressable Object | Unaddressable Object | Field.other | ADDRESS_NOT_VALID    |
      | Non-res              | Non-res              | Field.other | ADDRESS_NOT_VALID    |
      | Duplicate            | Duplicate            | Field.other | ADDRESS_NOT_VALID    |
      | Under Const          | Under Const          | Field.other | ADDRESS_NOT_VALID    |
      | CE - No contact      | CE - No contact      | Field.other | ADDRESS_TYPE_CHANGED |
      | CE - Contact made    | CE - Contact made    | Field.other | ADDRESS_TYPE_CHANGED |
  
  
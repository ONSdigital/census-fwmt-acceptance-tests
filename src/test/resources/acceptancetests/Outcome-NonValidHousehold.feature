@Census @Acceptance @Outcome @NonValidHousehold
Feature: Non-valid household Outcome Tests

  Scenario Outline: As a Gateway I can receive final Non Valid Household outcome of cases from TM and create Census Events
      Given TM sends a "<InputMessage>" Census Case Outcome to the Gateway
      And the Primary Outcome is "Non-valid household"
      And the Secondary Outcome is "<SecondaryOutcome>"
      When the Outcome Service process the message
      Then the Outcome Service should create a valid "<CaseEvent>" for the correct "<Topic>"
      And and of the correct "<EventType>" 
  
    Examples:
      | InputMessage         |  SecondaryOutcome     | CaseEvent              | Topic                     | EventType              |
      | Derelict             |  Derelict             | Gateway.Address.Update | event.case.address.update | ADDRESS_NOT_VALID      | 
      | Demolished           |  Demolished           | Gateway.Address.Update | event.case.address.update | ADDRESS_NOT_VALID      | 
      | Cant find            |  Cant find            | Gateway.Address.Update | event.case.address.update | ADDRESS_NOT_VALID      | 
      | Unaddressable Object |  Unaddressable Object | Gateway.Address.Update | event.case.address.update | ADDRESS_NOT_VALID      | 
      | Non-res              |  Non-res              | Gateway.Address.Update | event.case.address.update | ADDRESS_NOT_VALID      | 
      | Duplicate            |  Duplicate            | Gateway.Address.Update | event.case.address.update | ADDRESS_NOT_VALID      | 
      | Under Const          |  Under Const          | Gateway.Address.Update | event.case.address.update | ADDRESS_NOT_VALID      | 
      | CE - No contact      |  CE - No contact      | Gateway.Address.Update | event.case.address.update | ADDRESS_TYPE_CHANGED   | 
      | CE - Contact made    |  CE - Contact made    | Gateway.Address.Update | event.case.address.update | ADDRESS_TYPE_CHANGED   | 
  
  
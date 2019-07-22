@Census @Acceptance @Outcome @NonValidHousehold
Feature: Non-valid household Outcome Tests

  Scenario Outline: As a Gateway I can receive a CCS  outcome of cases from TM and create Census Events
    Given TM sends a "<InputMessage>" Census Case Outcome to the Gateway
    And the Primary Outcome is "<Primary Outcome>"
    And the Secondary Outcome is "<InputMessage>"
    When the Outcome Service process the message
    Then the Outcome Service should create a valid "<CaseEvent>" for the correct "<Topic>"
    And and of the correct "<EventType>"

    Examples:
      | InputMessage                | Primary Outcome             | CaseEvent                   | Topic                     | EventType         |
      | Complete on paper (full)    | Household                   | Gateway.CCS.Propertylisting | event.ccs.propertylisting | CCSPROPERTYLISTED |
      | Complete on paper (partial) | Household                   | Gateway.CCS.Propertylisting | event.ccs.propertylisting | CCSPROPERTYLISTED |
      | Soft refusal                | Household                   | Gateway.CCS.Propertylisting | event.ccs.propertylisting | CCSPROPERTYLISTED |
      | Hard refusal                | Household                   | Gateway.CCS.Propertylisting | event.ccs.propertylisting | CCSPROPERTYLISTED |
      | Extraordinary refusal       | Household                   | Gateway.CCS.Propertylisting | event.ccs.propertylisting | CCSPROPERTYLISTED |
      | Contact not needed          | Household                   | Gateway.CCS.Propertylisting | event.ccs.propertylisting | CCSPROPERTYLISTED |
      | Derelict / uninhabitable    | Household                   | Gateway.CCS.Propertylisting | event.ccs.propertylisting | CCSPROPERTYLISTED |
      | Potential Residential       | Household                   | Gateway.CCS.Propertylisting | event.ccs.propertylisting | CCSPROPERTYLISTED |
      | Under construction          | Household                   | Gateway.CCS.Propertylisting | event.ccs.propertylisting | CCSPROPERTYLISTED |
      | Soft refusal                | CE                          | Gateway.CCS.Propertylisting | event.ccs.propertylisting | CCSPROPERTYLISTED |
      | Hard refusal                | CE                          | Gateway.CCS.Propertylisting | event.ccs.propertylisting | CCSPROPERTYLISTED |
      | Extraordinary refusal       | CE                          | Gateway.CCS.Propertylisting | event.ccs.propertylisting | CCSPROPERTYLISTED |
      | Contact not needed          | CE                          | Gateway.CCS.Propertylisting | event.ccs.propertylisting | CCSPROPERTYLISTED |
      | Derelict / uninhabitable    | CE                          | Gateway.CCS.Propertylisting | event.ccs.propertylisting | CCSPROPERTYLISTED |
      | Potential Residential       | CE                          | Gateway.CCS.Propertylisting | event.ccs.propertylisting | CCSPROPERTYLISTED |
      | Under construction          | CE                          | Gateway.CCS.Propertylisting | event.ccs.propertylisting | CCSPROPERTYLISTED |
      | Non residential or business | Non residential or business | Gateway.CCS.Propertylisting | event.ccs.propertylisting | CCSPROPERTYLISTED |
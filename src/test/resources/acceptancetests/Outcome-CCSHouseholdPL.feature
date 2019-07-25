@Census @Acceptance @Outcome @CCSHouseholdPL
Feature: Non-valid household Outcome Tests

  Scenario Outline: As a Gateway I can receive a CCS PL outcome of cases from TM and create Census Events
    Given TM sends a "<InputMessage>" Census Case CCS PL Outcome to the Gateway
    And the Primary Outcome for CCS PL is "<Primary Outcome>"
    And the Secondary Outcome for CCS PL is "<InputMessage>"
    When the Outcome Service processes the CCS PL message
    Then the Outcome Service for the CCS PL should create a valid "<CaseEvent>" for the correct "<Topic>" and the outcome is of "<Outcome>"
    And and of the correct CCS "<EventType>"

    Examples:
      | InputMessage                | Primary Outcome | CaseEvent                   | Topic                     | EventType          | Outcome               |
#      | Complete on paper (full)    | Household       | Gateway.Ccs.Propertylisting | event.ccs.propertylisting | CCS_ADDRESS_LISTED | QUESTIONNAIRE_LINKED  |
#      | Complete on paper (partial) | Household       | Gateway.Ccs.Propertylisting | event.ccs.propertylisting | CCS_ADDRESS_LISTED | QUESTIONNAIRE_LINKED  |
      | Soft refusal                | Household       | Gateway.Ccs.Propertylisting | event.ccs.propertylisting | CCS_ADDRESS_LISTED | SOFT_REFUSAL          |
      | Hard refusal                | Household       | Gateway.Ccs.Propertylisting | event.ccs.propertylisting | CCS_ADDRESS_LISTED | HARD_REFUSAL          |
      | Extraordinary refusal       | Household       | Gateway.Ccs.Propertylisting | event.ccs.propertylisting | CCS_ADDRESS_LISTED | EXTRAORDINARY_REFUSAL |
#      | Contact not needed          | Household       | Gateway.Ccs.Propertylisting | event.ccs.propertylisting | CCS_ADDRESS_LISTED | CONTACT_NOT_NEEDED    |
#      | Derelict / Uninhabitable    | Household       | Gateway.Ccs.Propertylisting | event.ccs.propertylisting | CCS_ADDRESS_LISTED | DERELICT              |
#      | Potential Residential       | Household       | Gateway.Ccs.Propertylisting | event.ccs.propertylisting | CCS_ADDRESS_LISTED | POTENTIAL_RESIDENTIAL |
#      | Under construction          | Household       | Gateway.Ccs.Propertylisting | event.ccs.propertylisting | CCS_ADDRESS_LISTED | UNDER_CONSTRUCTION    |
#      | Soft refusal                | CE                          | Gateway.Ccs.Propertylisting | event.ccs.propertylisting | CCS_ADDRESS_LISTED |
#      | Hard refusal                | CE                          | Gateway.Ccs.Propertylisting | event.ccs.propertylisting | CCS_ADDRESS_LISTED |
#      | Extraordinary refusal       | CE                          | Gateway.Ccs.Propertylisting | event.ccs.propertylisting | CCS_ADDRESS_LISTED |
#      | Contact not needed          | CE                          | Gateway.Ccs.Propertylisting | event.ccs.propertylisting | CCS_ADDRESS_LISTED |
#      | Derelict / uninhabitable    | CE                          | Gateway.Ccs.Propertylisting | event.ccs.propertylisting | CCS_ADDRESS_LISTED |
#      | Potential Residential       | CE                          | Gateway.Ccs.Propertylisting | event.ccs.propertylisting | CCS_ADDRESS_LISTED |
#      | Under construction          | CE                          | Gateway.Ccs.Propertylisting | event.ccs.propertylisting | CCS_ADDRESS_LISTED |
#      | Non residential or business | Non residential or business | Gateway.Ccs.Propertylisting | event.ccs.propertylisting | CCS_ADDRESS_LISTED |
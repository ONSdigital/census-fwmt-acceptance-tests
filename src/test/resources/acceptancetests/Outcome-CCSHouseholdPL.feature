@Census @Acceptance @Outcome @CCSHouseholdPL
Feature: Non-valid household Outcome Tests

  Scenario Outline: As a Gateway I can receive a CCS PL outcome of cases from TM and create Census Events
    Given TM sends a "<InputMessage>" Census Case CCS PL Outcome to the Gateway with "<Primary Outcome>"
    And the Primary Outcome for CCS PL is "<Primary Outcome>"
    And the Secondary Outcome for CCS PL is "<InputMessage>"
    When the Outcome Service processes the CCS PL message
    Then the Outcome Service for the CCS PL should create a valid "<CaseEvent>" for the correct "<Topic>" and the outcome is of "<Outcome>"
    And and of the correct CCS "<EventType>"

    Examples:
      | InputMessage                | Primary Outcome            | CaseEvent                   | Topic                     | EventType          | Outcome               |
      | Complete on paper (full)    | Household                  | Gateway.Ccs.Propertylisting | event.ccs.propertylisting | CCS_ADDRESS_LISTED | 2420000002405886      |
      | Complete on paper (partial) | Household                  | Gateway.Ccs.Propertylisting | event.ccs.propertylisting | CCS_ADDRESS_LISTED | 1110000009            |
      | Soft refusal                | Household                  | Gateway.Ccs.Propertylisting | event.ccs.propertylisting | CCS_ADDRESS_LISTED | SOFT_REFUSAL          |
      | Hard refusal                | Household                  | Gateway.Ccs.Propertylisting | event.ccs.propertylisting | CCS_ADDRESS_LISTED | HARD_REFUSAL          |
      | Extraordinary refusal       | Household                  | Gateway.Ccs.Propertylisting | event.ccs.propertylisting | CCS_ADDRESS_LISTED | EXTRAORDINARY_REFUSAL |
      | Contact not needed          | Household                  | Gateway.Ccs.Propertylisting | event.ccs.propertylisting | CCS_ADDRESS_LISTED | Household             |
      | Derelict / uninhabitable    | Household                  | Gateway.Ccs.Propertylisting | event.ccs.propertylisting | CCS_ADDRESS_LISTED | DERELICT              |
      | Potential Residential       | Household                  | Gateway.Ccs.Propertylisting | event.ccs.propertylisting | CCS_ADDRESS_LISTED | Household             |
      | Under construction          | Household                  | Gateway.Ccs.Propertylisting | event.ccs.propertylisting | CCS_ADDRESS_LISTED | UNDER_CONSTRUCTION    |
      | Soft refusal                | CE                         | Gateway.Ccs.Propertylisting | event.ccs.propertylisting | CCS_ADDRESS_LISTED | SOFT_REFUSAL          |
      | Hard refusal                | CE                         | Gateway.Ccs.Propertylisting | event.ccs.propertylisting | CCS_ADDRESS_LISTED | HARD_REFUSAL          |
      | Extraordinary refusal       | CE                         | Gateway.Ccs.Propertylisting | event.ccs.propertylisting | CCS_ADDRESS_LISTED | EXTRAORDINARY_REFUSAL |
      | Collect CE details          | CE                         | Gateway.Ccs.Propertylisting | event.ccs.propertylisting | CCS_ADDRESS_LISTED | Care home             |
      | Contact not needed          | CE                         | Gateway.Ccs.Propertylisting | event.ccs.propertylisting | CCS_ADDRESS_LISTED | Care home             |
      | Derelict / uninhabitable    | CE                         | Gateway.Ccs.Propertylisting | event.ccs.propertylisting | CCS_ADDRESS_LISTED | DERELICT              |
      | Under construction          | CE                         | Gateway.Ccs.Propertylisting | event.ccs.propertylisting | CCS_ADDRESS_LISTED | UNDER_CONSTRUCTION    |
      | CE Out of scope             | CE                         | Gateway.Ccs.Propertylisting | event.ccs.propertylisting | CCS_ADDRESS_LISTED | CCS_CE_OUT_OF_SCOPE   |
      | Non residential / business  | Non residential / business | Gateway.Ccs.Propertylisting | event.ccs.propertylisting | CCS_ADDRESS_LISTED | NON_RESIDENTIAL       |
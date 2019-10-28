@Census @Acceptance @Outcome @CCSHouseholdPL
Feature: CCS Property Listing Outcome Tests

  Scenario Outline: As a Gateway I can receive a CCS PL outcome of cases from TM and create Census Events
    Given TM sends a "<InputMessage>" Census Case CCS PL Outcome to the Gateway with "<Primary Outcome>"
    And the Primary Outcome for CCS PL is "<Primary Outcome>"
    And the Secondary Outcome for CCS PL is "<InputMessage>"
    When the Outcome Service processes the CCS PL message
    Then the Outcome Service for the CCS PL should create a valid "<CaseEvent>"
    And and of the correct CCS "<EventType>"

    Examples:
      | InputMessage                | Primary Outcome            | CaseEvent   | EventType          |
      | Complete on paper (full)    | Household                  | Field.other | CCS_ADDRESS_LISTED |
      | Complete on paper (partial) | Household                  | Field.other | CCS_ADDRESS_LISTED |
      | Soft refusal                | Household                  | Field.other | CCS_ADDRESS_LISTED |
      | Hard refusal                | Household                  | Field.other | CCS_ADDRESS_LISTED |
      | Extraordinary refusal       | Household                  | Field.other | CCS_ADDRESS_LISTED |
      | Contact not needed          | Household                  | Field.other | CCS_ADDRESS_LISTED |
      | Derelict / Uninhabitable    | Household                  | Field.other | CCS_ADDRESS_LISTED |
      | Potential residential       | Household                  | Field.other | CCS_ADDRESS_LISTED |
      | Under construction          | Household                  | Field.other | CCS_ADDRESS_LISTED |
      | Soft refusal                | CE                         | Field.other | CCS_ADDRESS_LISTED |
      | Hard refusal                | CE                         | Field.other | CCS_ADDRESS_LISTED |
      | Extraordinary refusal       | CE                         | Field.other | CCS_ADDRESS_LISTED |
      | Collect CE details          | CE                         | Field.other | CCS_ADDRESS_LISTED |
      | Contact not needed          | CE                         | Field.other | CCS_ADDRESS_LISTED |
      | Derelict / Uninhabitable    | CE                         | Field.other | CCS_ADDRESS_LISTED |
      | Under construction          | CE                         | Field.other | CCS_ADDRESS_LISTED |
      | CE Out of scope             | CE                         | Field.other | CCS_ADDRESS_LISTED |
      | Non-residential or business  | Non residential or business | Field.other | CCS_ADDRESS_LISTED |
@Census @Acceptance
Feature: Census Tests

  Scenario: As Gateway I can receive a HouseHold create job requests from RM
    Given a TM doesnt have an existing job with case ID "39bad71c-7de5-4e1b-9a07-d9597737977f"
    And RM sends a create HouseHold job request with case ID of "39bad71c-7de5-4e1b-9a07-d9597737977f"
    When the Gateway sends a Create Job message to TM with case ID of "39bad71c-7de5-4e1b-9a07-d9597737977f"
    Then a new case with ID of "39bad71c-7de5-4e1b-9a07-d9597737977f" is created in TM

  Scenario: As Gateway I can receive NISRA Household create job requests from RM
    Given RM sends a create HouseHold job request job which has an field officer ID "allocatedOfficer"
    And the Gateway sends a Create Job message to TM with case ID of "39bad71c-7de5-4e1b-9a07-d9597737977f"
    Then a new case with id of "39bad71c-7de5-4e1b-9a07-d9597737977f" and allocated officer ID "allocatedOfficer" is created in TM

  Scenario Outline: As a system (FWMT Gateway) I can receive final outcome of cases from TM
    Given TM sends a "<InputMessage>" Census Case Outcome to the Gateway
    And the response is of a Census Case Outcome format
    And the response contains the Primary Outcome value of "<PrimaryOutcome>" and Secondary Outcome "<SecondaryOutcome>" and the Case ID of "6c9b1177-3e03-4060-b6db-f6a8456292ef"
    Then the message will made available for RM to pick up from queue "<ExpectedQueue>"
    And the message is in the format RM is expecting from queue "<ExpectedQueue>"

    Examples:
      | InputMessage | PrimaryOutcome     | SecondaryOutcome | ExpectedQueue              |
      | derelict     | No Valid Household | Derelict         | Gateway.Address.Update     |
      | hardRefusal  | Contact Made       | Hard Refusal     | Gateway.Respondent.Refusal |
      | splitAddress | Contact Made       | Split Address    | Gateway.Address.Update     |
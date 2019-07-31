@Census @Acceptance
Feature: NISRA Tests

  Scenario: As Gateway I can receive NISRA Household create job requests from RM
    Given RM sends a create HouseHold job request job which has a case ID of "39bad71c-7de5-4e1b-9a07-d9597737977f" and a field officer ID "allocatedOfficer"
    And the Gateway sends a Create Job message to TM with case ID of "39bad71c-7de5-4e1b-9a07-d9597737977f"
    Then a new case with id of "39bad71c-7de5-4e1b-9a07-d9597737977f" is created in TM

  Scenario: As Gateway I cannot receive NISRA Household create job requests from RM without a field officer ID
    Given RM sends a create HouseHold job request job which has a case ID of "39bad71c-7de5-4e1b-9a07-d9597737977f"
    Then RM will throw an exception for case ID "39bad71c-7de5-4e1b-9a07-d9597737977f"
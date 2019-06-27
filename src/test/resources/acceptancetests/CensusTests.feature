@Census @Acceptance
Feature: Census Tests

  Scenario: As Gateway I can receive a HouseHold create job requests from RM
    Given a TM doesnt have an existing job with case ID "39bad71c-7de5-4e1b-9a07-d9597737977f"
    And RM sends a create HouseHold job request
    When the Gateway sends a Create Job message to TM
    Then a new case with id of "39bad71c-7de5-4e1b-9a07-d9597737977f" is created in TM

  Scenario: As Gateway I can receive a HouseHold cancel job request from RM
    Given RM sends a cancel case Household job request with case ID "4d9294f6-8edc-4d32-99ad-1bdb485e3495"
    When the Gateway sends a Cancel Case request to TM with case ID "4d9294f6-8edc-4d32-99ad-1bdb485e3495"
    Then a pause datetime of "2030-01-01T00:00+00:00" will be assigned to the case with id "4d9294f6-8edc-4d32-99ad-1bdb485e3495"

  Scenario: As Gateway I cannot receive a non-HouseHold cancel job request from RM
    Given RM sends a cancel case CSS job request with case ID "81ec8f8e-1dfc-4b96-9bbd-c95f43ea0aa4" and receives an exception from RM
    Then the job with case ID "81ec8f8e-1dfc-4b96-9bbd-c95f43ea0aa4" will not be passed to TM

  Scenario: As Gateway I can receive a HouseHold update with pause job request from RM
    Given TM already has an existing job with case ID "39bad71c-7de5-4e1b-9a07-d9597737977f"
    And RM sends an update case job request with case ID "39bad71c-7de5-4e1b-9a07-d9597737977f"
    When the Gateway sends a Update Case with Pause request to TM with case ID "39bad71c-7de5-4e1b-9a07-d9597737977f"
    Then a pause datetime of "2019-05-27T00:00+00:00" with a reason "HQ Case Pause" will be assigned to the case with id "39bad71c-7de5-4e1b-9a07-d9597737977f"

  Scenario: As Gateway I can receive a reinstate HouseHold update with pause job request from RM
    Given TM already has an existing job with case ID "39bad71c-7de5-4e1b-9a07-d9597737977f" with a pause
    And RM sends an update case job request with case ID "39bad71c-7de5-4e1b-9a07-d9597737977f"
    When the Gateway sends a Update Case with a reinstate date case to TM with case ID "39bad71c-7de5-4e1b-9a07-d9597737977f"
    Then a pause datetime of "2019-05-27T00:00+00:00" with a reason "Case reinstated - blank QRE" will be assigned to the case with id "39bad71c-7de5-4e1b-9a07-d9597737977f"


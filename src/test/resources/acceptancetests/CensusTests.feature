@Census @Acceptance
Feature: Census Tests

  Scenario: As Gateway I can receive a HouseHold create job requests from RM
    Given a TM doesnt have an existing job with id "39bad71c-7de5-4e1b-9a07-d9597737977f"
    And RM sends a create HouseHold job request
    When the Gateway sends a Create Job message to TM
    Then a new case is created in TM

#  Scenario: As a system (FWMT Gateway) I can receive final outcome of cases from TM
#    Given TM sends a Census Case Outcome to the Gateway
#    And the response is of a Census Case Outcome format
#    And the response contains the Outcome value of "Will complete" and the Case Id of "6c9b1177-3e03-4060-b6db-f6a8456292ef"
#    Then the message will made available for RM to pick up
#    And the message is in the format RM is expecting




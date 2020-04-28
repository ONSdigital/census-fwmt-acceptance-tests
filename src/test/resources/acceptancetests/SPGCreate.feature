@Census @Acceptance @Create @SPG
Feature: SPG Outcome Tests

  Scenario: As Gateway I can receive a SPG Unit create job requests from RM
    Given a TM doesnt have an existing job with case ID "8dd42be3-09e6-488e-b4e2-0f14259acb9e"
    And RM sends a create HouseHold job request
    When the Gateway sends a Create Job message to TM
    Then a new case with id of "8dd42be3-09e6-488e-b4e2-0f14259acb9e" is created in TM
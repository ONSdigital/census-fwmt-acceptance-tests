@Census @Acceptance @Create @SPG
Feature: SPG Outcome Tests

  Scenario Outline: As Gateway I can receive a create job requests from RM
    Given a TM doesnt have a "<Survey>" "<Type>" job with case ID "8dd42be3-09e6-488e-b4e2-0f14259acb9e" in TM
    And RM sends a create HouseHold job request
    When the Gateway sends a Create Job message to TM
    Then a new case with id of "8dd42be3-09e6-488e-b4e2-0f14259acb9e" is created in TM
    Examples:
      |Survey  | Type |
      |SPG CE | Estab |
      |SPG CE | Unit |
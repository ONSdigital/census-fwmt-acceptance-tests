@Census @Acceptance @Create @SPG
Feature: SPG Create Tests

  Scenario Outline: As Gateway I can receive a create job requests from RM
    Given a TM doesnt have a "<Survey>" "<Type>" "<is_secure>" job with case ID "bd6345af-d706-43d3-a13b-8c549e081a76" in TM
    And RM sends a create SPG job request with caseRef "12345678"
    When the Gateway sends a Create Job message to TM
    Then a new case is created of the right type 
    And a new case with id of "bd6345af-d706-43d3-a13b-8c549e081a76" is created in TM
    Examples:
      |Survey  | Type | is_secure |
      |SPG CE | Estab |  T        |
      |SPG CE | Estab |  T        |
      |SPG CE | Estab |  F        |
      |SPG CE | Unit  |  N/A      |



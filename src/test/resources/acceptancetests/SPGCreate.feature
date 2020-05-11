@Census @Acceptance @Create @SPG
Feature: SPG Create Tests

  Scenario Outline: As Gateway I can receive a create job requests from RM
    Given a TM doesnt have a "<Survey>" "<Type>" job with case ID "bd6345af-d706-43d3-a13b-8c549e081a76" in TM
    And RM sends a create HouseHold job request
    When the Gateway sends a Create Job message to TM
    Then a new case with id of "bd6345af-d706-43d3-a13b-8c549e081a76" is created in TM
    Examples:
      |Survey  | Type |
      |SPG CE | Estab |
      |SPG CE | Unit |

  Scenario: As Gateway I can receive a update job requests from RM
    Given a job has been created in TM with case id "bd6345af-d706-43d3-a13b-8c549e081a76"
    And RM sends a update case request
    Then the update job is acknowledged by tm

  Scenario: As Gateway I can receive a cancel job requests from RM
    Given a job has been created in TM with case id "bd6345af-d706-43d3-a13b-8c549e081a76"
    And RM sends a cancel case request
    Then the cancel job is acknowledged by tm
@Census @Acceptance @Inbound @SPG
Feature: SPG Cancel Tests

  Scenario Outline: As Gateway I can receive a cancel job requests from RM for an existing job
    Given a TM doesnt have a job with case ID "bd6345af-d706-43d3-a13b-8c549e081a76" in TM
    And RM sends a create SPG job request with "<CaseRef>" "<Survey>" "<Type>" "<IsSecure>" "<HandDeliver>"
    And RM sends a cancel case request for the case 
    When Gateway receives a cancel message for the case
    Then it will Cancel the job with with the correct TM Action "<TmAction>" 
    And the cancel job is acknowledged by TM
   Examples:
      |Survey | Type  | IsSecure  | CaseRef  | HandDeliver | TmAction | 
      |SPG CE | Estab |  F        | 12345678 | F           | CLOSE    | 
      |SPG CE | Unit  |  F        | 12345678 | T           | CLOSE    | 

  Scenario: As Gateway I will fail when I receive a cancel job requests for RM from an unexisting job
    Given RM sends a cancel case request
    Then the cancel job should fail

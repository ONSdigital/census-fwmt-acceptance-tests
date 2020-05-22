@Census @Acceptance @Inbound @SPG
Feature: SPG Create Tests

  Scenario: As Gateway I can receive a cancel job requests from RM for an existing job
    Given a TM doesnt have a job with case ID "bd6345af-d706-43d3-a13b-8c549e081a76" in TM  
    And RM sends a create SPG job request with "12345678" "SPG CE" "Unit" "F" "T"
    And RM sends a cancel case request
    Then the cancel job is acknowledged by tm
 

  Scenario: As Gateway I will fail when I receive a cancel job requests for RM from an unexisting job
    Given RM sends a cancel case request
    Then the cancel job should fail

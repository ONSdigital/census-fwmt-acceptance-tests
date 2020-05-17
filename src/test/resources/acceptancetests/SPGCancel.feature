@Census @Acceptance @Inbound @SPG
Feature: SPG Create Tests

  Scenario: As Gateway I can receive a cancel job requests from RM for an existing job
    Given a job has been created a "SPG CE" "Unit" job in TM with case id "bd6345af-d706-43d3-a13b-8c549e081a76" with caseRef "12345678"
    And RM sends a cancel case request
    Then the cancel job is acknowledged by tm
 

  Scenario: As Gateway I will fail when I receive a cancel job requests for RM from an unexisting job
    Given RM sends a cancel case request
    Then the cancel job should fail

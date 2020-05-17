@Census @Acceptance @Inbound @SPG
Feature: SPG Create Tests

  Scenario Outline: As Gateway I can receive an update job request for SPG EST from RM
    Given a job has been created a "SPG CE" "<Type>" job in TM with case id "bd6345af-d706-43d3-a13b-8c549e081a76" with caseRef "12345678"
    And RM sends a update SPG job request 
    When the Gateway sends an Update Job message to TM
    Then the update job is acknowledged by tm
    Examples:
      | Type  |
      | Estab |
      | Unit  |

  Scenario: As Gateway I can receive an update job request for SPG Unit for an unexisting job and is set to undeliveredAsAddress. gateway will process as a Create message
    Given RM sends a update SPG Unit job request as undeliveredAsAddress "true" and case id "bd6345af-d706-43d3-a13b-8c549e081a76" with caseRef "12345678"
    Then Gateway will reroute it as a create message
    And Gateway will send a create job to TM
    And the create job is acknowledged by tm

  Scenario:  As Gateway I will fail an update job request for SPG Unit for an unexisting job when update is not undeliveredAsAddress
    Given RM sends a update SPG Unit job request as undeliveredAsAddress "false" and case id "bd6345af-d706-43d3-a13b-8c549e081a76" with caseRef "12345678"
    Then the update job should fail


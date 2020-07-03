@Census @Acceptance @Inbound @SPG
Feature: SPG Update Tests

  Scenario Outline: As Gateway I can receive an update to a job requests from RM for an existing job
    Given a TM doesnt have a job with case ID "bd6345af-d706-43d3-a13b-8c549e081a76" in TM
    And RM sends a create job request with "<CaseRef>" "<Survey>" "<Type>" "<IsSecure>" "<HandDeliver>"
    And RM sends an update case request for the case
    When Gateway receives an update message for the case
    Then it will update the job in TM
    And the updated job is acknowledged by TM
   Examples:
      | Survey | Type     | IsSecure  | CaseRef  | HandDeliver |
#      Not sure if SPG Estab is required as there are no specs on confluence. TBC
#      | SPG CE | Estab    |  F        | 12345678 | F           |
      | SPG CE | Unit     |  F        | 12345678 | F           |
      | SPG CE | Unit     |  F        | 12345678 | T           |
      | CE     | CE Est   |  F        | 12345678 | T           |
      | CE     | CE Est   |  F        | 12345678 | F           |
      | CE     | CE Unit  |  F        | 12345678 | T           |
      | CE     | CE Unit  |  F        | 12345678 | F           |

  Scenario Outline: As Gateway I can receive a create CE Site job request from RM after a CE Estab has been processed
    Given a TM doesnt have a job with case ID "bd6345af-d706-43d3-a13b-8c549e081a76" in TM
    And RM sends a create job request with "12345678" "CE" "CE Est" "F" "T"
    And RM sends a create CE Site job request with "<CaseRef>" "<Survey>" "<Type>" "<IsSecure>" "<HandDeliver>"
    When the Gateway sends a Create Job message to TM
    And RM sends an update case request for the case
    When Gateway receives an update message for the case
    Then it will update the job in TM
    And the updated job is acknowledged by TM
    Examples:
      |Survey | Type     | IsSecure | CaseRef  | HandDeliver |
      |CE     | CE Site  | F        | 12345678 | F           |
      |CE     | CE Site  | T        | 12345678 | F           |


  Scenario: As Gateway I will fail when I receive a Unit Update to a job requests from RM for a job that doesnt exist but undeliveredAsAddress is set to false
    Given RM sends a unit update case request where undeliveredAsAddress is "false"
    Then the update job should fail


  Scenario: As Gateway I can receive an update job request for SPG Unit for an unexisting job and is set to undeliveredAsAddress. gateway will process as a Create message
    Given RM sends a unit update case request where undeliveredAsAddress is "true"
    When Gateway receives an update message for the case
    Then Gateway will reroute it as a create message
    And Gateway will send a create job to TM
    And the create job is acknowledged by tm

 


@Census @Acceptance @Inbound @SPG
Feature: SPG Cancel Tests

  Scenario Outline: As Gateway I can receive a cancel job requests from RM for an existing job
    Given a TM doesnt have a job with case ID "bd6345af-d706-43d3-a13b-8c549e081a76" in TM
    And RM sends a create job request with "<CaseRef>" "<Survey>" "<Type>" "<IsSecure>" "<HandDeliver>"
    And RM sends a cancel case request for the case
    When Gateway receives a cancel message for the case
    Then it will Cancel the job with with the correct TM Action "<TmAction>"
    And the cancel job is acknowledged by TM
   Examples:
      |Survey  | Type    | IsSecure  | CaseRef  | HandDeliver | TmAction |
      | SPG CE | Estab   |  F        | 12345678 | F           | CLOSE    |
      | SPG CE | Unit    |  F        | 12345678 | T           | CLOSE    |
      | CE     | CE Est  |  F        | 12345678 | T           | CLOSE    |
      | CE     | CE Est  |  F        | 12345678 | F           | CLOSE    |
      | CE     | CE Unit |  F        | 12345678 | T           | CLOSE    |
      | CE     | CE Unit |  F        | 12345678 | F           | CLOSE    |
      | HH     | E&W     |  F        | 12345678 | T           | CLOSE    |
      | HH     | E&W     |  F        | 12345678 | F           | CLOSE    |

  Scenario Outline: As Gateway I can receive a cancel CE Site job request from RM after a CE Estab has been processed
    Given a TM doesnt have a job with case ID "bd6345af-d706-43d3-a13b-8c549e081a76" in TM
    And RM sends a create job request with "12345678" "CE" "CE Est" "F" "T"
    And RM sends a create CE Site job request with "<CaseRef>" "<Survey>" "<Type>" "<IsSecure>" "<HandDeliver>"
    And RM sends a cancel case request for the case
    When Gateway receives a cancel message for the case
    Then it will Cancel the job with with the correct TM Action "<TmAction>"
    And the cancel job is acknowledged by TM
    Examples:
      |Survey  | Type    | IsSecure  | CaseRef  | HandDeliver | TmAction |
      | CE     | CE Site |  F        | 12345678 | F           | CLOSE    |
      | CE     | CE Site |  T        | 12345678 | F           | CLOSE    |

  Scenario Outline: As Gateway I can receive a cancel NC job requests from RM
    Given TM has a related job with case ID "bd6345af-d706-43d3-a13b-8c549e081a76" for "<Original>" in TM
    And a TM doesnt have a job with case ID "e0b12e26-5a6d-11eb-ae93-0242ac130002" in TM
    And RM sends a create job request with "<CaseRef>" "<Survey>" "<Type>" "<IsSecure>" "<HandDeliver>"
    Then RM sends a NC cancel case request for the case with caseId "e0b12e26-5a6d-11eb-ae93-0242ac130002"
    When Gateway receives a NC cancel message for the case for caseId "bd6345af-d706-43d3-a13b-8c549e081a76"
    Then it will Cancel the job with with the correct TM Action "<TmAction>"
    And the NC cancel job for "bd6345af-d706-43d3-a13b-8c549e081a76" is acknowledged by TM
    Examples:
      | Original | Survey | Type    | IsSecure | CaseRef  | HandDeliver | TmAction |
      | CE Est   | CE     | NC CE   | F        | 12345678 | F           | CLOSE    |
      | HH       | HH     | NC HH   | F        | 12345678 | F           | CLOSE    |

  Scenario Outline: As Gateway I can receive a cancel CCS Int job request from RM after a CE Estab has been processed
    Given RM sends a create job request with "12345678" "CCS" "CCS_PL" "F" "F"
    And RM sends a create job request with "<CaseRef>" "<Survey>" "<Type>" "<IsSecure>" "<HandDeliver>"
    And RM sends a cancel case request for the case
    When Gateway receives a cancel message for the case
    Then it will Cancel the job with with the correct TM Action "<TmAction>"
    And the cancel job is acknowledged by TM
    Examples:
      | Survey | Type       | IsSecure | CaseRef  | HandDeliver | TmAction |
      | CCS    | CCS Int CE | F        | 12345678 | F           | CLOSE    |
      | CCS    | CCS Int HH | F        | 12345678 | F           | CLOSE    |

  Scenario: As Gateway I can deal with an internal a feedback cancel job request for an existing job
    Given a TM doesnt have a job with case ID "bd6345af-d706-43d3-a13b-8c549e081a76" in TM
    And RM sends a create job request with "12345678" "CE" "CE Est" "F" "F"
    And RM sends a feedback cancel case request for the case
    When Gateway receives a cancel message for the case
    Then it will Cancel the job with with the correct TM Action "CLOSE"
    And the cancel job is acknowledged by TM

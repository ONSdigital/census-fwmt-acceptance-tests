@Census @Acceptance @Inbound @Create
Feature: Create Tests

  Scenario Outline: As Gateway I can receive a create job requests from RM
    Given a TM doesnt have a job with case ID "bd6345af-d706-43d3-a13b-8c549e081a76" in TM
    And RM sends a create job request with "<CaseRef>" "<Survey>" "<Type>" "<IsSecure>" "<HandDeliver>"
    When the Gateway sends a Create Job message to TM
    Then a new case is created of the right "<SurveyType>"
    And the right caseRef "<TmCaseRef>"
    And a new case with id of "bd6345af-d706-43d3-a13b-8c549e081a76" is created in TM
    Examples:
      | MessageTypeLabel            | Survey | Type    | IsSecure | CaseRef  | HandDeliver | SurveyType | TmCaseRef      |
      | SPG Site                    | SPG CE | Estab   | F        | 12345678 | F           | SPG Site   | 12345678       |
      | SPG Site (Secure)           | SPG CE | Estab   | T        | 12345678 | F           | SPG Site   | SECSS_12345678 |
      | SPG Unit Deliver            | SPG CE | Unit    | F        | 12345678 | T           | SPG Unit-D | 12345678       |
      | SPG Unit Follow-up          | SPG CE | Unit    | F        | 12345678 | F           | SPG Unit-F | 12345678       |
      | SPG Unit Follow-up (Secure) | SPG CE | Unit    | T        | 12345678 | F           | SPG Unit-F | SECSU_12345678 |
      | CE Est Deliver              | CE     | CE Est  | F        | 12345678 | T           | CE Est-D   | 12345678       |
      | CE Est Deliver (Secure)     | CE     | CE Est  | T        | 12345678 | T           | CE Est-D   | SECCE_12345678 |
      | CE Est Follow-up            | CE     | CE Est  | F        | 12345678 | F           | CE Est-F   | 12345678       |
      | CE Est Follow-up (Secure)   | CE     | CE Est  | T        | 12345678 | F           | CE Est-F   | SECCE_12345678 |
      | CE Unit Deliver             | CE     | CE Unit | F        | 12345678 | T           | CE Unit-D  | 12345678       |
      | CE Unit Deliver (Secure)    | CE     | CE Unit | T        | 12345678 | T           | CE Unit-D  | SECCU_12345678 |
      | CE Unit Follow-up           | CE     | CE Unit | F        | 12345678 | F           | CE Unit-F  | 12345678       |
      | CE Unit Follow-up (Secure)  | CE     | CE Unit | T        | 12345678 | F           | CE Unit-F  | SECCU_12345678 |
      | Household England and Wales | HH     | E&W     | F        | 12345678 | F           | HH         | 12345678       |
      | Household Nisra             | HH     | NISRA   | F        | 12345678 | F           | HH         | 12345678       |
      | CCS Property Listing        | CCS    | CCS_PL  | F        | 12345678 | F           | CCS PL     | 12345678       |

  Scenario Outline: As Gateway I can receive a NC create job requests from RM
    Given TM has a related job with case ID "bd6345af-d706-43d3-a13b-8c549e081a76" for "<Original>" in TM
    And a TM doesnt have a job with case ID "e0b12e26-5a6d-11eb-ae93-0242ac130002" in TM
    And RM sends a create job request with "<CaseRef>" "<Survey>" "<Type>" "<IsSecure>" "<HandDeliver>"
    When the Gateway sends a Create Job message to TM
    Then a new case is created of the right "<SurveyType>"
    And the right caseRef "<TmCaseRef>"
    And a new case with id of "e0b12e26-5a6d-11eb-ae93-0242ac130002" is created in TM
    Examples:
      | Original | Survey | Type    | IsSecure | CaseRef  | HandDeliver | SurveyType | TmCaseRef      |
      | CE Est   | CE     | NC CE   | F        | 12345678 | F           | NC         | 12345678       |
      | HH       | HH     | NC HH   | F        | 12345678 | F           | NC         | 12345678       |

  Scenario Outline: As Gateway I can receive a create CE Site job request from RM after a CE Estab has been processed
    Given a TM doesnt have a job with case ID "bd6345af-d706-43d3-a13b-8c549e081a76" in TM
    And RM sends a create job request with "12345678" "CE" "CE Est" "F" "T"
    And RM sends a create CE Site job request with "<CaseRef>" "<Survey>" "<Type>" "<IsSecure>" "<HandDeliver>"
    When the Gateway sends a Create Job message to TM
    Then a new case is created of the right "<SurveyType>"
    And the right caseRef "<TmCaseRef>"
    And a new case with id of "bd6345af-d706-43d3-a13b-8c549e081a76" is created in TM
    Examples:
      | Survey | Type    | IsSecure | CaseRef  | HandDeliver | SurveyType | TmCaseRef      |
      | CE     | CE Site | F        | 12345678 | F           | CE Site    | 12345678       |
      | CE     | CE Site | T        | 12345678 | F           | CE Site    | SECCS_12345678 |
      | CE     | CE Site | F        | 12345678 | T           | CE Site    | 12345678       |
      | CE     | CE Site | T        | 12345678 | T           | CE Site    | SECCS_12345678 |

  Scenario Outline: As Gateway I can switch a CE survey type that has a matching estabUprn and address type
    Given a TM doesnt have a job with case ID "bd6345af-d706-43d3-a13b-8c549e081a76" in TM
    And RM sends a create job request with "12345678" "CE" "CE Est" "F" "T"
    And RM sends a create CE Unit with the same estabUPRN as the above CE Est request with "<CaseRef>" "<Survey>" "<Type>" "<IsSecure>" "<HandDeliver>"
    Then the existing case is updated to a switch and put back on the queue with caseId "bd6345af-d706-43d3-a13b-8c549e081a76"
    Then the related case will be closed with case ID "bd6345af-d706-43d3-a13b-8c549e081a76"
    And then reopened with the new SurveyType "<SurveyType>" and case ID "bd6345af-d706-43d3-a13b-8c549e081a76"
    Examples:
      | Survey | Type    | IsSecure | HandDeliver | CaseRef  | SurveyType |
      | CE     | CE Unit | F        | T           | 12345678 | CE Site    |
      | CE     | CE Unit | F        | F           | 12345678 | CE Site    |
      | CE     | CE Unit | T        | F           | 12345678 | CE Site    |
      | CE     | CE Unit | T        | T           | 12345678 | CE Site    |

  Scenario Outline: As Gateway I can receive CCS Int create job requests from RM
    Given a TM doesnt have a job with case ID "bd6345af-d706-43d3-a13b-8c549e081a76" in TM
    And RM sends a create job request with "12345678" "CCS" "CCS_PL" "F" "F"
    And RM sends a create job request with "<CaseRef>" "<Survey>" "<Type>" "<IsSecure>" "<HandDeliver>"
    When the Gateway sends a Create Job message to TM
    And a new case with id of "bd6345af-d706-43d3-a13b-8c549e081a76" and with the correct survey type "<SurveyType>" is created in TM
    Examples:
      | Survey | Type       | IsSecure | CaseRef  | HandDeliver | SurveyType |
      | CCS    | CCS Int CE | F        | 12345678 | F           | CCS INT    |
      | CCS    | CCS Int HH | F        | 12345678 | F           | CCS INT    |
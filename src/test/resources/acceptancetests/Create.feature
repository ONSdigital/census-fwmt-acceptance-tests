@Census @Acceptance @Create @SPG
Feature: SPG Create Tests

  Scenario Outline: As Gateway I can receive a create job requests from RM
    Given a TM doesnt have a job with case ID "bd6345af-d706-43d3-a13b-8c549e081a76" in TM
    And RM sends a create SPG job request with "<CaseRef>" "<Survey>" "<Type>" "<IsSecure>" "<HandDeliver>"
    When the Gateway sends a Create Job message to TM
    Then a new case is created of the right "<SurveyType>"
    And the right caseRef "<TmCaseRef>"
    And a new case with id of "bd6345af-d706-43d3-a13b-8c549e081a76" is created in TM
    Examples:
      |Survey | Type     | IsSecure | CaseRef  | HandDeliver | SurveyType | TmCaseRef      |
      |SPG CE | Estab    | F        | 12345678 | F           | SPG Site   | 12345678       |
      |SPG CE | Estab    | T        | 12345678 | F           | SPG Site   | SECSS_12345678 |
      |SPG CE | Unit     | F        | 12345678 | T           | SPG Unit-D | 12345678       |
      |SPG CE | Unit     | F        | 12345678 | F           | SPG Unit-F | 12345678       |
      |SPG CE | Unit     | T        | 12345678 | F           | SPG Unit-F | SECSU_12345678 |
      |CE     | CE Est   | F        | 12345678 | T           | CE Est-D   | 12345678       |
      |CE     | CE Est   | T        | 12345678 | T           | CE Est-D   | SECCE_12345678 |
      |CE     | CE Est   | F        | 12345678 | F           | CE Est-F   | 12345678       |
      |CE     | CE Est   | T        | 12345678 | F           | CE Est-F   | SECCE_12345678 |
      |CE     | CE Unit  | F        | 12345678 | T           | CE Unit-D  | 12345678       |
      |CE     | CE Unit  | T        | 12345678 | T           | CE Unit-D  | SECCU_12345678 |
      |CE     | CE Unit  | F        | 12345678 | F           | CE Unit-F  | 12345678       |
      |CE     | CE Unit  | T        | 12345678 | F           | CE Unit-F  | SECCU_12345678 |


#Add failing route checks like in Cancel


@Census @Acceptance
Feature: Census Tests

  Scenario: As Gateway I can receive a HouseHold create job requests from RM
    Given a TM doesnt have an existing job with case ID "39bad71c-7de5-4e1b-9a07-d9597737977f"
    And RM sends a create HouseHold job request
    When the Gateway sends a Create Job message to TM
    Then a new case with id of "39bad71c-7de5-4e1b-9a07-d9597737977f" is created in TM

  Scenario Outline: As a system (FWMT Gateway) I can receive final outcome of cases from TM
    Given TM sends a "<InputMessage>" Census Case Outcome to the Gateway
    And the response is of a Census Case Outcome format
    And the response contains the Primary Outcome value of "<PrimaryOutcome>" and Secondary Outcome "<SecondaryOutcome>" and the Case Id of "6c9b1177-3e03-4060-b6db-f6a8456292ef"
    Then the message will made available for RM to pick up from queue "<ExpectedQueue>"
    And the message is in the format RM is expecting from queue "<ExpectedQueue>"

    Examples:
      | InputMessage | PrimaryOutcome     | SecondaryOutcome | ExpectedQueue              |
      | derelict     | No Valid Household | Derelict         | Gateway.Address.Update     |
      | hardRefusal  | Contact Made       | Hard Refusal     | Gateway.Respondent.Refusal |
      | splitAddress | Contact Made       | Split Address    | Gateway.Address.Update     |

  Scenario: As Gateway I can receive a HouseHold cancel job request from RM
    Given RM sends a cancel case Household job request with case ID "4d9294f6-8edc-4d32-99ad-1bdb485e3495"
    When the Gateway sends a Cancel Case request to TM with case ID "4d9294f6-8edc-4d32-99ad-1bdb485e3495"
    Then a pause datetime of "2030-01-01T00:00+00:00" will be assigned to the case with id "4d9294f6-8edc-4d32-99ad-1bdb485e3495"

  Scenario: As Gateway I cannot send a non-HouseHold cancel job request from RM
    Given RM sends a cancel case CSS job request with case ID "81ec8f8e-1dfc-4b96-9bbd-c95f43ea0aa4"
    Then the job with case ID "81ec8f8e-1dfc-4b96-9bbd-c95f43ea0aa4" will not be passed to TM


  Scenario Outline: As a system (FWMT Gateway) I can handle fulfilment requests of the following Secondary Outcome:
    Given TM sends a "<InputMessage>" Census Case Outcome to the Gateway
    And the response is of a Census Case Outcome format
    And the response contains the Primary Outcome value of "Contact Made" and Secondary Outcome "<SecondaryOutcome>" and the Case Id of "6c9b1177-3e03-4060-b6db-f6a8456292ef"
    Then the message will made available for RM to pick up from queue "Gateway.Fulfillment.Request"
    And the message is in the format RM is expecting from queue "Gateway.Fulfillment.Request"

    Examples:
    | InputMessage                    | SecondaryOutcome                  |
    | willComplete                    | Will Complete                     |
    | haveCompleted                   | Have Completed                    |
    | collectedCompletedQuestionnaire | Collected completed questionnaire |
    | callBackAnotherTime             | Call back another time            |
    | holidayHome                     | Holiday home                      |
    | secondResidence                 | Second residence                  |
    | requestedAssistance             | Requested assistance              |

@Census @Acceptance
Feature: Census Tests

  Scenario: As Gateway I can receive a HouseHold create job requests from RM
    Given a TM doesnt have an existing job with case ID "39bad71c-7de5-4e1b-9a07-d9597737977f"
    And RM sends a create HouseHold job request
    When the Gateway sends a Create Job message to TM
    Then a new case with id of "39bad71c-7de5-4e1b-9a07-d9597737977f" is created in TM

  Scenario Outline: As a Gateway I can receive final outcome of cases from TM
    Given TM sends a "<InputMessage>" Census Case Outcome to the Gateway with case ID "6c9b1177-3e03-4060-b6db-f6a8456292ef"
    And the response is of a Census Case Outcome format
    And the response contains the Primary Outcome value of "<PrimaryOutcome>" and Secondary Outcome "<SecondaryOutcome>" and the Case Id of "6c9b1177-3e03-4060-b6db-f6a8456292ef"
    Then the message will made available for RM to pick up from queue "<ExpectedQueue>"
    And the message is in the format RM is expecting from queue "<ExpectedQueue>"

    Examples:
      | InputMessage | PrimaryOutcome     | SecondaryOutcome                    | ExpectedQueue              |
      | derelict     | No Valid Household | Derelict                            | Gateway.Address.Update     |
      | ceNoContact  | No Valid Household | Property is a CE - no contact made  | Gateway.Address.Update     |
      | ceContact    | No Valid Household | Property is a CE - Contact made     | Gateway.Address.Update     |
      | hardRefusal  | Contact Made       | Hard Refusal                        | Gateway.Respondent.Refusal |
      | splitAddress | Contact Made       | Split Address                       | Gateway.Address.Update     |

  Scenario: As Gateway I can receive a HouseHold cancel job request from RM
    Given RM sends a cancel case Household job request with case ID "4d9294f6-8edc-4d32-99ad-1bdb485e3495"
    When the Gateway sends a Cancel Case request to TM with case ID "4d9294f6-8edc-4d32-99ad-1bdb485e3495"
    Then a pause datetime of "2030-01-01T00:00+00:00" will be assigned to the case with id "4d9294f6-8edc-4d32-99ad-1bdb485e3495"

  Scenario: As Gateway I cannot receive a non-HouseHold cancel job request from RM
    Given RM sends a cancel case CSS job request with case ID "81ec8f8e-1dfc-4b96-9bbd-c95f43ea0aa4" and receives an exception from RM
    Then the job with case ID "81ec8f8e-1dfc-4b96-9bbd-c95f43ea0aa4" will not be passed to TM

  Scenario: As Gateway I can receive a HouseHold update with pause job request from RM
    Given TM already has an existing job with case ID "39bad71c-7de5-4e1b-9a07-d9597737977f"
    And RM sends an update case job request with case ID "39bad71c-7de5-4e1b-9a07-d9597737977f"
    When the Gateway sends a Update Case with Pause request to TM with case ID "39bad71c-7de5-4e1b-9a07-d9597737977f"
    Then a pause datetime of "2019-05-27T00:00+00:00" with a reason "HQ Case Pause" will be assigned to the case with id "39bad71c-7de5-4e1b-9a07-d9597737977f"

  Scenario: As Gateway I can receive a reinstate HouseHold update with pause job request from RM
    Given TM already has an existing job with case ID "39bad71c-7de5-4e1b-9a07-d9597737977f" with a pause
    And RM sends an update case job request with case ID "39bad71c-7de5-4e1b-9a07-d9597737977f"
    When the Gateway sends a Update Case with a reinstate date case to TM with case ID "39bad71c-7de5-4e1b-9a07-d9597737977f"
    Then a pause datetime of "2019-05-27T00:00+00:00" with a reason "Case reinstated - blank QRE" will be assigned to the case with id "39bad71c-7de5-4e1b-9a07-d9597737977f"

  Scenario Outline: As a Gateway I can handle fulfilment requests of the following Secondary Outcome:
    Given TM sends a "<InputMessage>" Census Case Outcome to the Gateway with case ID "6c9b1177-3e03-4060-b6db-f6a8456292ef"
    Given the response is of a Census Case Outcome format
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

  Scenario: As a Gateway I can ensure that Secondary fulfillment requests pass on the QID to RM
    Given TM sends a "holidayHome" Census Case Outcome to the Gateway with case ID "6c9b1177-3e03-4060-b6db-f6a8456292ef"
    And the response contains the QuestionnaireId "QuestionnaireID" from queue "Gateway.Fulfillment.Request"

  Scenario Outline: As a Gateway I can handle multiple fulfilment requests for questionnaires by post
    Given TM sends a "<InputMessage>" Census Case Outcome to the Gateway with case ID "6c9b1177-3e03-4060-b6db-f6a8456292ef"
    And the response is of a Census Case Outcome format
    And the response contains the Primary Outcome value of "Contact Made" and Secondary Outcome "<SecondaryOutcome>" and the Case Id of "6c9b1177-3e03-4060-b6db-f6a8456292ef"
    Then the message will made available for RM to pick up from queue "Gateway.Fulfillment.Request"
    And the message is in the format RM is expecting from queue "Gateway.Fulfillment.Request"

    Examples:
      | InputMessage                 | SecondaryOutcome                       |
      | householdPaperRequest        | Paper H Questionnaire required by post |
      | householdContinuationRequest | Paper H Questionnaire required by post |
      | householdIndividualRequest   | Paper H Questionnaire required by post |

  Scenario: As a Gateway I can ensure that Individual paper requests pass on requester details to RM
    Given TM sends a "householdIndividualRequest" Census Case Outcome to the Gateway with case ID "6c9b1177-3e03-4060-b6db-f6a8456292ef"
    And the response contains the Requester Title "Mr" and Requester Forename "Hugh" and Requester Surname "Mungus" from queue "Gateway.Fulfillment.Request"

  Scenario: As a Gateway I can receive outcomes with multiple paper request fulfillment requests and pass them all to RM
    Given TM sends a "multipleQuestionnaireRequest" Census Case Outcome to the Gateway with case ID "6c9b1177-3e03-4060-b6db-f6a8456292ef"
    Then the number of messages "3" will made available for RM to pick up from queue "Gateway.Fulfillment.Request"

  Scenario: As a Gateway I can handle fulfilment requests of HUAC required by text:
    Given TM sends a "huacRequiredByText" Census Case Outcome to the Gateway with case ID "6c9b1177-3e03-4060-b6db-f6a8456292ef"
    And the response is of a Census Case Outcome format
    And the response contains the Primary Outcome value of "Contact Made" and Secondary Outcome "HUAC required by text" and the Case Id of "6c9b1177-3e03-4060-b6db-f6a8456292ef"
    Then the message will made available for RM to pick up from queue "Gateway.Fulfillment.Request"
    And the message is in the format RM is expecting from queue "Gateway.Fulfillment.Request"

  Scenario: As a Gateway I can handle fulfilment requests of IUAC required by text:
    Given TM sends a "iuacRequiredByText" Census Case Outcome to the Gateway with case ID "6c9b1177-3e03-4060-b6db-f6a8456292ef"
    And the response is of a Census Case Outcome format
    And the response contains the Primary Outcome value of "Contact Made" and Secondary Outcome "IUAC required by text" and the Case Id of "6c9b1177-3e03-4060-b6db-f6a8456292ef"
    Then the message will made available for RM to pick up from queue "Gateway.Fulfillment.Request"
    And the message is in the format RM is expecting from queue "Gateway.Fulfillment.Request"

  Scenario: As a Gateway I can ensure that the UAC fulfillment requests pass on details to RM
    Given TM sends a "iuacRequiredByText" Census Case Outcome to the Gateway with case ID "6c9b1177-3e03-4060-b6db-f6a8456292ef"
    And the response contains the Requestor Phone Number "07123456789" from queue "Gateway.Fulfillment.Request"

  Scenario: As a Gateway I can receive a CSV file for CE and can pass the content to TM
    Given the Gateway receives a CSV CE with case ID "2f1ea0fd-18b1-4786-b1f7-3e9a79ed1a52"
    When the Gateway sends a Create Job message to TM with case ID "2f1ea0fd-18b1-4786-b1f7-3e9a79ed1a52"
    And TM picks up the Create Job message with case ID "2f1ea0fd-18b1-4786-b1f7-3e9a79ed1a52"
    Then a new case with id of "2f1ea0fd-18b1-4786-b1f7-3e9a79ed1a52" is created in TM



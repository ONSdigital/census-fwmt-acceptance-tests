@Census @Acceptance @Outcome
Feature: Outcome Tests

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
      
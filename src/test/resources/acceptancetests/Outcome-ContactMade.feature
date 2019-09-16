@Census @Acceptance @Outcome @ContactMade
Feature: Contact made Outcome Tests

  Scenario Outline: As a Gateway I can receive final Contact Made outcome of cases from TM and create Census Events
    Given TM sends a "<InputMessage>" Census Case Outcome to the Gateway where "<QuestionnareId has a value>"
    And the Primary Outcome is of "Contact made"
    And the Secondary Outcome is of "<SecondaryOutcome>"
    When the Outcome Service process the message
    Then a valid "<CaseEvent>"
    And of the correct "<EventType>"

    Examples:
      | InputMessage                             | QuestionnareId has a value | SecondaryOutcome                         | CaseEvent      | EventType            |
      | Will complete                            | true                       | Will complete                            | Field.other    | QUESTIONNAIRE_LINKED |
      | Will complete                            | false                      | Will complete                            | Field.other    | FULFILMENT_REQUESTED |
      | Have completed                           | true                       | Have completed                           | Field.other    | QUESTIONNAIRE_LINKED |
      | Have completed                           | false                      | Have completed                           | Field.other    | FULFILMENT_REQUESTED |
      | Collect completed questionnaire          | true                       | Collect completed questionnaire          | Field.other    | QUESTIONNAIRE_LINKED |
      | Collect completed questionnaire          | false                      | Collect completed questionnaire          | Field.other    | FULFILMENT_REQUESTED |
      | Asked to call back another time          | true                       | Asked to call back another time          | Field.other    | QUESTIONNAIRE_LINKED |
      | Asked to call back another time          | false                      | Asked to call back another time          | Field.other    | FULFILMENT_REQUESTED |
      | Holiday home                             | true                       | Holiday home                             | Field.other    | QUESTIONNAIRE_LINKED |
      | Holiday home                             | false                      | Holiday home                             | Field.other    | FULFILMENT_REQUESTED |
      | Second residence                         | true                       | Second residence                         | Field.other    | QUESTIONNAIRE_LINKED |
      | Second residence                         | false                      | Second residence                         | Field.other    | FULFILMENT_REQUESTED |
      | Requested assistance                     | true                       | Requested assistance                     | Field.other    | QUESTIONNAIRE_LINKED |
      | Requested assistance                     | false                      | Requested assistance                     | Field.other    | FULFILMENT_REQUESTED |
      | Paper H questionnaire required by post   | true                       | Paper H questionnaire required by post   | Field.other    | QUESTIONNAIRE_LINKED |
      | Paper H questionnaire required by post   | false                      | Paper H questionnaire required by post   | Field.other    | FULFILMENT_REQUESTED |
      | Paper H questionnaire issued on doorstep | true                       | Paper H questionnaire issued on doorstep | Field.other    | QUESTIONNAIRE_LINKED |
      | Paper H questionnaire issued on doorstep | false                      | Paper H questionnaire issued on doorstep | Field.other    | FULFILMENT_REQUESTED |
      | HUAC required by text                    | true                       | HUAC required by text                    | Field.other    | QUESTIONNAIRE_LINKED |
      | HUAC required by text                    | false                      | HUAC required by text                    | Field.other    | FULFILMENT_REQUESTED |
      | Split address                            | false                      | Split address                            | Field.other    | ADDRESS_NOT_VALID    |
      | Hard refusal                             | false                      | Hard refusal                             | Field.refusals | REFUSAL_RECEIVED     |
      | Extraordinary refusal                    | false                      | Extraordinary refusal                    | Field.refusals | REFUSAL_RECEIVED     |

  Scenario: As Gateway I can receive final Contact Made outcome with multiple fulfilment requests
    Given TM sends a Contact Made Census Case Outcome to the Gateway
    And the message contains "3" fulfilment requests
    When the Outcome Service process the message
    Then the service should create "3" messages
    And the messages should be correct

  Scenario: As Gateway I can receive final Contact Made outcome with multiple Questionnaire Linked requests
    Given TM sends a Questionnaire Linked Contact Made Census Case Outcome to the Gateway
    And the message contains "3" fulfilment requests
    When the Outcome Service process the message
    Then the service should create "3" messages
    And the Questionnaire Linked messages should be correct

  Scenario: As Gateway I can receive final Contact Made outcome with multiple fulfilment requests
    Given TM sends a Mixed Contact Made Census Case Outcome to the Gateway
    And the message contains "3" fulfilment requests
    When the Outcome Service process the message
    Then the service should create "3" messages
    And the Mixed messages should be correct

  Scenario: As Gateway I cannot receive final Contact Made outcome with no fulfilment request
    Given TM sends a blank fulfillment request and receives an error from Outcome Service

    
    
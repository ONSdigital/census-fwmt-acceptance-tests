@Census @Acceptance @Outcome @ContactMade
Feature: Contact made Outcome Tests

  Scenario Outline: As a Gateway I can receive final Contact Made outcome of cases from TM and create Census Events
      Given TM sends a "<InputMessage>" Census Case Outcome to the Gateway where "<QuestionnareId has a value>"
      And the Primary Outcome is "Contact made"
      And the Secondary Outcome is "<SecondaryOutcome>"
      When the Outcome Service process the message
      Then the Outcome Service should create a valid "<CaseEvent>" for the correct "<Topic>"
      And and of the correct "<EventType>" 
  
    Examples:
      | InputMessage                             | QuestionnareId has a value |  SecondaryOutcome                         | CaseEvent                   | Topic                     | EventType              |
      | Will complete                            | true                       |  Will complete                            | Gateway.Fulfillment.Request | event.fulfillment.request | QUESTIONNAIRE_LINKED   | 
      | Will complete                            | false                      |  Will complete                            | Gateway.Fulfillment.Request | event.fulfillment.request | FULFILMENT_REQUESTED   | 
      | Have completed                           | true                       |  Have completed                           | Gateway.Fulfillment.Request | event.fulfillment.request | QUESTIONNAIRE_LINKED   | 
      | Have completed                           | false                      |  Have completed                           | Gateway.Fulfillment.Request | event.fulfillment.request | FULFILMENT_REQUESTED   | 
      | Collect completed questionnaire          | true                       |  Collect completed questionnaire          | Gateway.Fulfillment.Request | event.fulfillment.request | QUESTIONNAIRE_LINKED   | 
      | Collect completed questionnaire          | false                      |  Collect completed questionnaire          | Gateway.Fulfillment.Request | event.fulfillment.request | FULFILMENT_REQUESTED   | 
      | Asked to call back another time          | true                       |  Asked to call back another time          | Gateway.Fulfillment.Request | event.fulfillment.request | QUESTIONNAIRE_LINKED   | 
      | Asked to call back another time          | false                      |  Asked to call back another time          | Gateway.Fulfillment.Request | event.fulfillment.request | FULFILMENT_REQUESTED   | 
      | Holiday home                             | true                       |  Holiday home                             | Gateway.Fulfillment.Request | event.fulfillment.request | QUESTIONNAIRE_LINKED   | 
      | Holiday home                             | false                      |  Holiday home                             | Gateway.Fulfillment.Request | event.fulfillment.request | FULFILMENT_REQUESTED   | 
      | Second residence                         | true                       |  Second residence                         | Gateway.Fulfillment.Request | event.fulfillment.request | QUESTIONNAIRE_LINKED   | 
      | Second residence                         | false                      |  Second residence                         | Gateway.Fulfillment.Request | event.fulfillment.request | FULFILMENT_REQUESTED   | 
      | Requested assistance                     | true                       |  Requested assistance                     | Gateway.Fulfillment.Request | event.fulfillment.request | QUESTIONNAIRE_LINKED   | 
      | Requested assistance                     | false                      |  Requested assistance                     | Gateway.Fulfillment.Request | event.fulfillment.request | FULFILMENT_REQUESTED   | 
      | Paper H questionnaire required by post   | true                       |  Paper H questionnaire required by post   | Gateway.Fulfillment.Request | event.fulfillment.request | QUESTIONNAIRE_LINKED   | 
      | Paper H questionnaire required by post   | false                      |  Paper H questionnaire required by post   | Gateway.Fulfillment.Request | event.fulfillment.request | FULFILMENT_REQUESTED   | 
      | Paper H questionnaire issued on doorstep | true                       |  Paper H questionnaire issued on doorstep | Gateway.Fulfillment.Request | event.fulfillment.request | QUESTIONNAIRE_LINKED   | 
      | Paper H questionnaire issued on doorstep | false                      |  Paper H questionnaire issued on doorstep | Gateway.Fulfillment.Request | event.fulfillment.request | FULFILMENT_REQUESTED   | 
      | HUAC required by text                    | true                       |  HUAC required by text                    | Gateway.Fulfillment.Request | event.fulfillment.request | QUESTIONNAIRE_LINKED   | 
      | HUAC required by text                    | false                      |  HUAC required by text                    | Gateway.Fulfillment.Request | event.fulfillment.request | FULFILMENT_REQUESTED   | 
      | Split address                            | false                      |  Split address                            | Gateway.Address.Update      | event.case.address.update | ADDRESS_NOT_VALID      | 
      | Hard refusal                             | false                      |  Hard refusal                             | Gateway.Respondent.Refusal  | event.respondent.refusal  | REFUSAL_RECEIVED       | 
      | Extraordinary refusal                    | false                      |  Extraordinary refusal                    | Gateway.Respondent.Refusal  | event.respondent.refusal  | REFUSAL_RECEIVED       | 
  
  Scenario: As Gateway I can receive final Contact Made outcome with multiple fulfilment requests
    Given TM sends a Contact Made Census Case Outcome to the Gateway
    And the message contains "3" fulfilment requests
    When the Outcome Service process the message
    Then the Outcome Service should create "3" messages 
    And the messages should be correct
    
  Scenario: As Gateway I can receive final Contact Made outcome with multiple fulfilment requests
    Given TM sends a Questionnaire Linked Contact Made Census Case Outcome to the Gateway
    And the message contains "3" fulfilment requests
    When the Outcome Service process the message
    Then the Outcome Service should create "3" messages 
    And the Questionnaire Linked messages should be correct 
    
  Scenario: As Gateway I can receive final Contact Made outcome with multiple fulfilment requests
    Given TM sends a Mixed Contact Made Census Case Outcome to the Gateway
    And the message contains "3" fulfilment requests
    When the Outcome Service process the message
    Then the Outcome Service should create "3" messages 
    And the Mixed messages should be correct 
    
    
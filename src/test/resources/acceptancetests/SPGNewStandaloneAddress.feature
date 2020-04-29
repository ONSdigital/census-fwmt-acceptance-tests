@Census @Acceptance @Outcome @SPG @NewStandaloneAddress
Feature: SPG Outcome Tests

  Scenario Outline: As a Gateway I can receive a SPG outcomes of cases from TM and create Census Events
    Given the Field Officer sends a "<Type>"
    And the Primary Outcome is "<Primary Outcome>"
    And the secondary Outcome "<Secondary Outcome>"
    And Outcome code is "<Outcome Code>"
    When Gateway receives SPG outcome
    Then It will send an "<Operation List>" messages to RM
    And each message conforms to "<Output Message List>"

 # SPG NEW STANDALONE ADDRESS move and edit
#      | REFUSAL_RECEIVED | Access Granted - Contact made | Hard refusal          | 10-20-02      | REFUSAL_RECEIVED | REFUSAL_RECEIVED    |
#      | REFUSAL_RECEIVED | Access Granted - Contact made | Extraordinary refusal | 10-20-03      | REFUSAL_RECEIVED | REFUSAL_RECEIVED    |
#      | LINKED_QID           | Access Granted - Contact made | HICL or Paper H Questionnaire delivered   | 10-20-05     | LINKED_QID              | LINKED_QID                                |
#      | FULFILMENT_REQUESTED | Access Granted - Contact made           | HUAC required by text                     | 10-20-04      | FULFILMENT_REQUESTED    | FULFILMENT_REQUESTED                      |

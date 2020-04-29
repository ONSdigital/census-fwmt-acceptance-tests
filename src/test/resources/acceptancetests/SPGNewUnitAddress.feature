@Census @Acceptance @Outcome @SPG @NewUnitAddress
Feature: SPG Outcome Tests

  Scenario Outline: As a Gateway I can receive a SPG outcomes of cases from TM and create Census Events
    Given the Field Officer sends a "<Type>"
    And the Primary Outcome is "<Primary Outcome>"
    And the secondary Outcome "<Secondary Outcome>"
    And Outcome code is "<Outcome Code>"
    When Gateway receives SPG outcome
    Then It will send an "<Operation List>" messages to RM
    And each message conforms to "<Output Message List>"

        # new unit move and edit
#      | FULFILMENT_REQUESTED | Contact made           | HUAC required by text                     | 9-20-04      | FULFILMENT_REQUESTED    | FULFILMENT_REQUESTED                      |
#      | LINKED_QID           | Contact made           | HICL or Paper H Questionnaire delivered   | 9-20-05      | LINKED_QID              | LINKED_QID                                |

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


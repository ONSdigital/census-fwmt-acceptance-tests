@Census @Acceptance @Outcome @SPG
Feature: SPG Outcome Tests

  # think i will need a test for each type on its own to account for case id not being supplied to new unit and new standalone

  Scenario Outline: As a Gateway I can receive a SPG outcomes of cases from TM and create Census Events
    Given the Field Officer sends a "<Type>"
    And the Primary Outcome is "<Primary Outcome>"
    And the secondary Outcome "<Secondary Outcome>"
    And Outcome code is "<Outcome Code>"
    When Gateway receives the outcome
    Then It will send an "<Operation List>" messages to RM
    And each message conforms to "<Output Message List>"

    Examples:
      | Type              | Primary Outcome        | Secondary Outcome              | Outcome Code | Operation List    | Output Message List |
      | ADDRESS_NOT_VALID | Engagement - Not-valid | Visit - Derelict or demolished | 6-10-56      | ADDRESS_NOT_VALID | ADDRESS_NOT_VALID   |



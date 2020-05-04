@Census @Acceptance @SPG @Feedback
Feature: SPG Outcome Tests

  Scenario Outline: As a Gateway I can receive an SPG outcome which will provide feedback to tm
    Given a job has been created in TM with case id "8dd42be3-09e6-488e-b4e2-0f14259acb9e"
    And tm sends a "<Type>" outcome
    Then a "<input>" feedback message is sent to tm
    And "<output>" is acknowledged by tm

    Examples:
      | Type               | input             | output           |
      | CANCEL_FEEDBACK    | COMET_CANCEL_SENT | COMET_CANCEL_ACK |
      | DELIVERED_FEEDBACK | COMET_UPDATE_SENT | COMET_UPDATE_ACK |
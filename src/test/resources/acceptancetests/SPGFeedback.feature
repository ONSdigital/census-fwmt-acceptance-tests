@Census @Acceptance @SPG @Feedback
Feature: SPG Outcome Tests

  Scenario Outline: As a Gateway I can receive an SPG outcome which will provide feedback to tm
    Given a job has been created in TM with case id "<caseId>"
    And tm sends a "<Type>" outcome
    Then a "<Type>" feedback message is sent to tm
    And "<eventType>" is acknowledged by tm
    Examples:
      | caseId                               | Type               | eventType |
      | 8dd42be3-09e6-488e-b4e2-0f14259acb9e | CANCEL_FEEDBACK    | CANCEL    |
      | 8dd42be3-09e6-488e-b4e2-0f14259acb9e | DELIVERED_FEEDBACK | UPDATE    |
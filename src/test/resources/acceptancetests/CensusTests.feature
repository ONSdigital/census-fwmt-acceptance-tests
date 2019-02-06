@Census @Acceptance
Feature: Census Tests

  Scenario: As a system (FWMT Gateway) I can receive final outcome of cases from TM
    Given TM sends a Census case outcome to the Job Service
    And the response is an Census job
    And the response contains the outcome and caseId
    Then the message will be put on the queue to RM
    And the message is in the RM composite format

  Scenario: As Gateway I can receive create job requests (Household, Census Coverage Survey & communal establishments) from RM
    Given a job with the id "jobId" doesn't exist
    And RM sends a create job request
    When the gateway sends a create message to TM
    Then a new case is created in TM

  Scenario: Non Functional: If a message is in an invalid format from RM, it is logged via Splunk & stored in a dead letter queue
    Given a message in an invalid format from RM
    Then the error is logged via SPLUNK & stored in a queue "Action.FieldDLQ"

  Scenario: Non Functional: If a message is in an invalid format from TM, it is logged via splunk & stored in a dead letter queue
    Given a message in an invalid format from TM
    Then the error is logged via SPLUNK & stored in a queue "Gateway.FeedbackDLQ"

  Scenario: If TM goes down when a message is in transit and the message has failed to send 3 times then it is stored on a dead letter queue & splunk
    Given a message received from RM that fails to send to TM after 3 attempts
    Then the error is logged via SPLUNK & stored in a queue "Gateway.ActionsDLQ"



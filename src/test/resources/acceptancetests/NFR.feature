Scenario: Non Functional: If a message is in an invalid format from RM, it is logged via Splunk & stored in a dead letter queue
Given a message in an invalid format from RM
Then the error is logged via SPLUNK & stored in a queue "Action.FieldDLQ"

Scenario: Non Functional: If a message is in an invalid format from TM, it is logged via splunk & stored in a dead letter queue
Given a message in an invalid format from TM
Then the error is logged via SPLUNK & stored in a queue "Gateway.FeedbackDLQ"

Scenario: If TM goes down when a message is in transit and the message has failed to send 3 times then it is stored on a dead letter queue & splunk
Given a message received from RM that fails to send to TM after 3 attempts
Then the error is logged via SPLUNK & stored in a queue "Gateway.ActionsDLQ"

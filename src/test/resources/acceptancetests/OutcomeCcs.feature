@Census @Acceptance @Outcome @SPG @CE
Feature: Outcome New Address Reported Tests

  Scenario Outline: As a Gateway I can receive an outcome from TM and create Census Events
    Given a property list case exists
    And an "<SurveyType>" "<BusinessFunction>" outcome message
    And its Primary Outcome is "<Primary Outcome>"
    And its secondary Outcome "<Secondary Outcome>"
    And its Outcome code is "<Outcome Code>"
    And the message includes a Linked QID "<HasLinkedQID>"
    And the message includes a Fulfillment Request "<HasFulfilmentRequest>"
    When Gateway receives the outcome
    Then It will run the following processors "<Operation List>"
    And create the following messages to RM "<RmMessages>"
    And the caseId of the "<InitialOperation>" message will be a new caseId
    And every other message will use the new caseId as its caseId
    And each message has the correct values
    And it will create the following messages "<JsMessages>" to JobService

   Examples:
   | SurveyType | BusinessFunction         | Primary Outcome   | Secondary Outcome      | Outcome Code | HasLinkedQID | HasFulfilmentRequest | Operation List                   | RmMessages                               | InitialOperation      |JsMessages        |
   | CCS PL     | Property Listed HH       | Contact Made      | Property Listed        | 30-01-02     | F            | F                    | PROPERTY_LISTED_HH,LINKED_QID    | CCS_ADDRESS_LISTED                       | CCS_ADDRESS_LISTED    |                  |
   | CCS PL     | Property Listed HH       | Contact Made      | Property Listed        | 30-01-02     | T            | F                    | PROPERTY_LISTED_HH,LINKED_QID    | CCS_ADDRESS_LISTED,QUESTIONNAIRE_LINKED  | CCS_ADDRESS_LISTED    |                  |
   | CCS PL     | Property Listed CE       | Contact Made      | Property Listed        | 30-02-02     | F            | F                    | PROPERTY_LISTED_CE               | CCS_ADDRESS_LISTED                       | CCS_ADDRESS_LISTED    |                  |
   | CCS PL     | Interview Required HH    | Contact Made      | Property Listed        | 30-01-01     | F            | F                    | INTERVIEW_REQUIRED_HH            | CCS_ADDRESS_LISTED                       | CCS_ADDRESS_LISTED    |                  |
   | CCS PL     | Interview Required CE    | Contact Made      | Property Listed        | 30-02-01     | F            | F                    | INTERVIEW_REQUIRED_CE            | CCS_ADDRESS_LISTED                       | CCS_ADDRESS_LISTED    |                  |

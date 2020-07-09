@Census @Acceptance @Outcome @SPG @CE
Feature: Outcome Tests

  Scenario Outline: As a Gateway I can receive an outcome from TM and create Census Events
    Given an "<SurveyType>" "<BusinessFunction>" outcome message
    And its Primary Outcome is "<Primary Outcome>"
    And its secondary Outcome "<Secondary Outcome>"
    And its Outcome code is "<Outcome Code>"
    And the message includes a Linked QID "<HasLinkedQID>"
    And the message includes a Fulfillment Request "<HasFulfilmentRequest>"
    When Gateway receives the outcome
    Then It will run the following processors "<Operation List>"
    And create the following messages to RM "<RmMessages>"
    And each message has the correct values
    And it will create the following messages "<JsMessages>" to JobService

   Examples:
    | SurveyType | BusinessFunction         | Primary Outcome   | Secondary Outcome                | Outcome Code | HasLinkedQID | HasFulfilmentRequest | Operation List                                                                   | RmMessages                                                       | JsMessages       |
    | SPG        | Not Valid Address        | Not Valid         | Visit - Unoccupied Site          | 6-30-03      | F            | F                    | ADDRESS_NOT_VALID,CANCEL_FEEDBACK,FULFILMENT_REQUESTED,LINKED_QID                | ADDRESS_NOT_VALID                                                | CANCEL           |
    | SPG        | Not Valid Address        | Not Valid         | Visit - Unoccupied Site          | 6-30-03      | T            | F                    | ADDRESS_NOT_VALID,CANCEL_FEEDBACK,FULFILMENT_REQUESTED,LINKED_QID                | ADDRESS_NOT_VALID,QUESTIONNAIRE_LINKED                           | CANCEL           |
    | SPG        | Not Valid Address        | Not Valid         | Visit - Unoccupied Site          | 6-30-03      | F            | T                    | ADDRESS_NOT_VALID,CANCEL_FEEDBACK,FULFILMENT_REQUESTED,LINKED_QID                | ADDRESS_NOT_VALID,FULFILMENT_REQUESTED                           | CANCEL           |
    | SPG        | Not Valid Address        | Not Valid         | Visit - Unoccupied Site          | 6-30-03      | T            | T                    | ADDRESS_NOT_VALID,CANCEL_FEEDBACK,FULFILMENT_REQUESTED,LINKED_QID                | ADDRESS_NOT_VALID,QUESTIONNAIRE_LINKED,FULFILMENT_REQUESTED      | CANCEL           |

    | SPG        | Hard Refusal             | Contact Made      | Phone - Hard Refusal             | 6-20-04      | F            | F                    | HARD_REFUSAL_RECEIVED,CANCEL_FEEDBACK,FULFILMENT_REQUESTED,LINKED_QID            | REFUSAL_RECEIVED                                                 | CANCEL           |
    | SPG        | Hard Refusal             | Contact Made      | Phone - Hard Refusal             | 6-20-04      | T            | F                    | HARD_REFUSAL_RECEIVED,CANCEL_FEEDBACK,FULFILMENT_REQUESTED,LINKED_QID            | REFUSAL_RECEIVED,QUESTIONNAIRE_LINKED                            | CANCEL           |
    | SPG        | Hard Refusal             | Contact Made      | Phone - Hard Refusal             | 6-20-04      | F            | T                    | HARD_REFUSAL_RECEIVED,CANCEL_FEEDBACK,FULFILMENT_REQUESTED,LINKED_QID            | REFUSAL_RECEIVED,FULFILMENT_REQUESTED                            | CANCEL           |
    | SPG        | Hard Refusal             | Contact Made      | Phone - Hard Refusal             | 6-20-04      | T            | T                    | HARD_REFUSAL_RECEIVED,CANCEL_FEEDBACK,FULFILMENT_REQUESTED,LINKED_QID            | REFUSAL_RECEIVED,QUESTIONNAIRE_LINKED,FULFILMENT_REQUESTED       | CANCEL           |

    | SPG        | Extraordinary Refusal    | Contact Made      | Phone - Extraordinary Refusal    | 6-20-05      | F            | F                    | EXTRAORDINARY_REFUSAL_RECEIVED,CANCEL_FEEDBACK,FULFILMENT_REQUESTED,LINKED_QID   | REFUSAL_RECEIVED                                                 | CANCEL           |
    | SPG        | Extraordinary Refusal    | Contact Made      | Phone - Extraordinary Refusal    | 6-20-05      | T            | F                    | EXTRAORDINARY_REFUSAL_RECEIVED,CANCEL_FEEDBACK,FULFILMENT_REQUESTED,LINKED_QID   | REFUSAL_RECEIVED,QUESTIONNAIRE_LINKED                            | CANCEL           |
    | SPG        | Extraordinary Refusal    | Contact Made      | Phone - Extraordinary Refusal    | 6-20-05      | F            | T                    | EXTRAORDINARY_REFUSAL_RECEIVED,CANCEL_FEEDBACK,FULFILMENT_REQUESTED,LINKED_QID   | REFUSAL_RECEIVED,FULFILMENT_REQUESTED                            | CANCEL           |
    | SPG        | Extraordinary Refusal    | Contact Made      | Phone - Extraordinary Refusal    | 6-20-05      | T            | T                    | EXTRAORDINARY_REFUSAL_RECEIVED,CANCEL_FEEDBACK,FULFILMENT_REQUESTED,LINKED_QID   | REFUSAL_RECEIVED,QUESTIONNAIRE_LINKED,FULFILMENT_REQUESTED       | CANCEL           |


#    | SPG        | New Unit Reported        | Contact Made                  | Visit another time                      | 9-20-01      | T            | T                    | NEW_UNIT_ADDRESS,FULFILMENT_REQUESTED,LINKED_QID                                 | NEW_ADDRESS_REPORTED,QUESTIONNAIRE_LINKED,FULFILMENT_REQUESTED |                  |
#    | SPG        | New Standalone Address   | Access Granted - Contact Made | HUAC required by text                   | 10-20-04     | T            | T                    | NEW_ADDRESS_REPORTED,FULFILMENT_REQUESTED,LINKED_QID                             | NEW_ADDRESS_REPORTED,QUESTIONNAIRE_LINKED,FULFILMENT_REQUESTED |                  |
#    | SPG        | Cancel Feedback          | Contact Made                  | HICL or Paper H Questionnaire delivered | 22-20-05     | T            | T                    | CANCEL_FEEDBACK,FULFILMENT_REQUESTED,LINKED_QID                                  | QUESTIONNAIRE_LINKED,FULFILMENT_REQUESTED                      | CANCEL           |
#    | SPG        | Delivered Feedback       | Contact Made                  | HUAC required by text                   | 7-20-04      | T            | T                    | CANCEL_FEEDBACK,DELIVERED_FEEDBACK,FULFILMENT_REQUESTED,LINKED_QID               | QUESTIONNAIRE_LINKED,FULFILMENT_REQUESTED                      | CANCEL,UPDATE    |

#    | SPG        | No Action         | Irrelavant        | Irrelavant        | 6-20-02      | F            | F                    |                |            |            |


    | CE         | Not Valid Address        | Not Valid         | Visit - Unoccupied Site          | 20-10-05     | F            | F                    | ADDRESS_NOT_VALID,FULFILMENT_REQUESTED,LINKED_QID                                | ADDRESS_NOT_VALID                                                |                  |
    | CE         | Not Valid Address        | Not Valid         | Visit - Unoccupied Site          | 20-10-05     | T            | F                    | ADDRESS_NOT_VALID,FULFILMENT_REQUESTED,LINKED_QID                                | ADDRESS_NOT_VALID,QUESTIONNAIRE_LINKED                           |                  |
    | CE         | Not Valid Address        | Not Valid         | Visit - Unoccupied Site          | 20-10-05     | F            | T                    | ADDRESS_NOT_VALID,FULFILMENT_REQUESTED,LINKED_QID                                | ADDRESS_NOT_VALID,FULFILMENT_REQUESTED                           |                  |
    | CE         | Not Valid Address        | Not Valid         | Visit - Unoccupied Site          | 20-10-05     | T            | T                    | ADDRESS_NOT_VALID,FULFILMENT_REQUESTED,LINKED_QID                                | ADDRESS_NOT_VALID,QUESTIONNAIRE_LINKED,FULFILMENT_REQUESTED      |                  |

    | CE         | Hard Refusal             | Contact Made      | Phone - Hard Refusal             | 20-20-04     | F            | F                    | HARD_REFUSAL_RECEIVED,CANCEL_FEEDBACK,FULFILMENT_REQUESTED,LINKED_QID            | REFUSAL_RECEIVED                                                 | CANCEL           |
    | CE         | Hard Refusal             | Contact Made      | Phone - Hard Refusal             | 20-20-04     | T            | F                    | HARD_REFUSAL_RECEIVED,CANCEL_FEEDBACK,FULFILMENT_REQUESTED,LINKED_QID            | REFUSAL_RECEIVED,QUESTIONNAIRE_LINKED                            | CANCEL           |
    | CE         | Hard Refusal             | Contact Made      | Phone - Hard Refusal             | 20-20-04     | F            | T                    | HARD_REFUSAL_RECEIVED,CANCEL_FEEDBACK,FULFILMENT_REQUESTED,LINKED_QID            | REFUSAL_RECEIVED,FULFILMENT_REQUESTED                            | CANCEL           |
    | CE         | Hard Refusal             | Contact Made      | Phone - Hard Refusal             | 20-20-04     | T            | T                    | HARD_REFUSAL_RECEIVED,CANCEL_FEEDBACK,FULFILMENT_REQUESTED,LINKED_QID            | REFUSAL_RECEIVED,QUESTIONNAIRE_LINKED,FULFILMENT_REQUESTED       | CANCEL           |

    | CE         | Extraordinary Refusal    | Contact Made      | Phone - Extraordinary Refusal    | 20-20-03     | F            | F                    | EXTRAORDINARY_REFUSAL_RECEIVED,CANCEL_FEEDBACK,FULFILMENT_REQUESTED,LINKED_QID   | REFUSAL_RECEIVED                                                 | CANCEL           |
    | CE         | Extraordinary Refusal    | Contact Made      | Phone - Extraordinary Refusal    | 20-20-03     | T            | F                    | EXTRAORDINARY_REFUSAL_RECEIVED,CANCEL_FEEDBACK,FULFILMENT_REQUESTED,LINKED_QID   | REFUSAL_RECEIVED,QUESTIONNAIRE_LINKED                            | CANCEL           |
    | CE         | Extraordinary Refusal    | Contact Made      | Phone - Extraordinary Refusal    | 20-20-03     | F            | T                    | EXTRAORDINARY_REFUSAL_RECEIVED,CANCEL_FEEDBACK,FULFILMENT_REQUESTED,LINKED_QID   | REFUSAL_RECEIVED,FULFILMENT_REQUESTED                            | CANCEL           |
    | CE         | Extraordinary Refusal    | Contact Made      | Phone - Extraordinary Refusal    | 20-20-03     | T            | T                    | EXTRAORDINARY_REFUSAL_RECEIVED,CANCEL_FEEDBACK,FULFILMENT_REQUESTED,LINKED_QID   | REFUSAL_RECEIVED,QUESTIONNAIRE_LINKED,FULFILMENT_REQUESTED       | CANCEL           |


#    | CE         | New Unit Reported        | Contact Made                  | Visit another time                      | 9-20-01      | T            | T                    | NEW_UNIT_ADDRESS,FULFILMENT_REQUESTED,LINKED_QID                                 | NEW_ADDRESS_REPORTED,QUESTIONNAIRE_LINKED,FULFILMENT_REQUESTED |                  |
#    | CE         | New Standalone Address   | Access Granted - Contact Made | HUAC required by text                   | 10-20-04     | T            | T                    | NEW_ADDRESS_REPORTED,FULFILMENT_REQUESTED,LINKED_QID                             | NEW_ADDRESS_REPORTED,QUESTIONNAIRE_LINKED,FULFILMENT_REQUESTED |                  |
#    | CE         | Cancel Feedback          | Contact Made                  | HICL or Paper H Questionnaire delivered | 22-20-05     | T            | T                    | CANCEL_FEEDBACK,FULFILMENT_REQUESTED,LINKED_QID                                  | QUESTIONNAIRE_LINKED,FULFILMENT_REQUESTED                      | CANCEL           |
#    | CE         | Delivered Feedback       | Contact Made                  | HUAC required by text                   | 7-20-04      | T            | T                    | CANCEL_FEEDBACK,DELIVERED_FEEDBACK,FULFILMENT_REQUESTED,LINKED_QID               | QUESTIONNAIRE_LINKED,FULFILMENT_REQUESTED                      | CANCEL,UPDATE    |

#    | CE         | No Action         | Irrelavant        | Irrelavant        | 6-20-02      | F            | F                    |                |            |            |

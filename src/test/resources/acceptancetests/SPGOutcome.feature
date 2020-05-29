@Census @Acceptance @Outcome @SPG
Feature: SPG Outcome Tests

  Scenario Outline: As a Gateway I can receive a SPG outcomes of cases from TM and create Census Events
    Given the Field Officer sends a "<Type>"
    And the Primary Outcome is "<Primary Outcome>"
    And the secondary Outcome "<Secondary Outcome>"
    And Outcome code is "<Outcome Code>"
    When Gateway receives SPG outcome
    Then It will send an "<Operation List>" messages to RM
    And each message conforms to "<Output Message List>"

    Examples:
      | Type                    | Primary Outcome        | Secondary Outcome                         | Outcome Code | Operation List                           | Output Message List                       |

      # FULFILMENT REQUESTED code change

      | FULFILMENT_REQUESTED | Contact made           | HUAC required by text                     | 7-20-04      | FULFILMENT_REQUESTED    | FULFILMENT_REQUESTED                      |
      | FULFILMENT_REQUESTED | Contact made           | HUAC required by text                     | 8-20-04      | FULFILMENT_REQUESTED    | FULFILMENT_REQUESTED                      |

      # LINKED QID work

      | LINKED_QID           | No contact             | HICL or Paper H Questionnaire delivered   | 7-30-04      | LINKED_QID              | LINKED_QID                                |
      | LINKED_QID           | Contact made           | HICL or Paper H Questionnaire delivered   | 7-20-05      | LINKED_QID              | LINKED_QID                                |
      | LINKED_QID           | Contact made           | HICL or Paper H Questionnaire delivered   | 8-20-05      | LINKED_QID              | LINKED_QID                                |

      # ADDRESS TYPE CHANGE work (did work, caching issue)

      | ADDRESS_TYPE_CHANGED_HH | Engagement - Not-valid | Phone - Property is a household - refused | 6-10-04      | ADDRESS_TYPE_CHANGED_HH,REFUSAL_RECEIVED | ADDRESS_TYPE_CHANGED_HH,REFUSAL_RECEIVED  |
      | ADDRESS_TYPE_CHANGED_HH | Engagement - Not-valid | Phone - Property is a household           | 6-10-03      | ADDRESS_TYPE_CHANGED_HH                  | ADDRESS_TYPE_CHANGED_HH                   |
      | ADDRESS_TYPE_CHANGED_CE | Engagement - Not-valid | Phone - Property is a CE - refused        | 6-10-02      | ADDRESS_TYPE_CHANGED_CE,REFUSAL_RECEIVED | ADDRESS_TYPE_CHANGED_CE,REFUSAL_RECEIVED  |
      | ADDRESS_TYPE_CHANGED_CE | Engagement - Not-valid | Phone - Property is a CE                  | 6-10-01      | ADDRESS_TYPE_CHANGED_CE                  | ADDRESS_TYPE_CHANGED_CE                   |

      | ADDRESS_TYPE_CHANGED_HH | Engagement - Not-valid | Visit - Property is a household - refused | 6-10-54      | ADDRESS_TYPE_CHANGED_HH,REFUSAL_RECEIVED | ADDRESS_TYPE_CHANGED_HH, REFUSAL_RECEIVED |
      | ADDRESS_TYPE_CHANGED_HH | Engagement - Not-valid | Visit - Property is a household           | 6-10-53      | ADDRESS_TYPE_CHANGED_HH                  | ADDRESS_TYPE_CHANGED_HH                   |
      | ADDRESS_TYPE_CHANGED_CE | Engagement - Not-valid | Visit - Property is a CE - refused        | 6-10-52      | ADDRESS_TYPE_CHANGED_CE,REFUSAL_RECEIVED | ADDRESS_TYPE_CHANGED_CE,REFUSAL_RECEIVED  |
      | ADDRESS_TYPE_CHANGED_CE | Engagement - Not-valid | Visit - Property is a CE                  | 6-10-51      | ADDRESS_TYPE_CHANGED_CE                  | ADDRESS_TYPE_CHANGED_CE                   |

      | ADDRESS_TYPE_CHANGED_HH | Not-valid       | Visit - Property is a household - refused | 7-10-04      | ADDRESS_TYPE_CHANGED_HH,REFUSAL_RECEIVED | ADDRESS_TYPE_CHANGED_HH,REFUSAL_RECEIVED |
      | ADDRESS_TYPE_CHANGED_HH | Not-valid       | Visit - Property is a household           | 7-10-03      | ADDRESS_TYPE_CHANGED_HH                  | ADDRESS_TYPE_CHANGED_HH                  |
      | ADDRESS_TYPE_CHANGED_CE | Not-valid       | Visit - Property is a CE                  | 7-10-01      | ADDRESS_TYPE_CHANGED_CE                  | ADDRESS_TYPE_CHANGED_CE                  |
      | ADDRESS_TYPE_CHANGED_CE | Not-valid       | Visit - Property is a CE - refused        | 7-10-02      | ADDRESS_TYPE_CHANGED_CE,REFUSAL_RECEIVED | ADDRESS_TYPE_CHANGED_CE,REFUSAL_RECEIVED |

      | ADDRESS_TYPE_CHANGED_HH | Not-valid       | Visit - Property is a household - refused | 8-10-04      | ADDRESS_TYPE_CHANGED_HH,REFUSAL_RECEIVED | ADDRESS_TYPE_CHANGED_HH,REFUSAL_RECEIVED |
      | ADDRESS_TYPE_CHANGED_HH | Not-valid       | Visit - Property is a household           | 8-10-03      | ADDRESS_TYPE_CHANGED_HH                  | ADDRESS_TYPE_CHANGED_HH                  |
      | ADDRESS_TYPE_CHANGED_CE | Not-valid       | Visit - Property is a CE                  | 8-10-01      | ADDRESS_TYPE_CHANGED_CE                  | ADDRESS_TYPE_CHANGED_CE                  |
      | ADDRESS_TYPE_CHANGED_CE | Not-valid       | Visit - Property is a CE - refused        | 8-10-02      | ADDRESS_TYPE_CHANGED_CE,REFUSAL_RECEIVED | ADDRESS_TYPE_CHANGED_CE,REFUSAL_RECEIVED |

      # REFUSALS works

      | REFUSAL_RECEIVED | Engagement - Contact made | Phone - Hard refusal          | 6-20-04      | REFUSAL_RECEIVED | REFUSAL_RECEIVED    |
      | REFUSAL_RECEIVED | Engagement - Contact made | Phone - Extraordinary refusal | 6-20-05      | REFUSAL_RECEIVED | REFUSAL_RECEIVED    |
      | REFUSAL_RECEIVED | Engagement - Contact made | Visit - Hard refusal          | 6-20-53      | REFUSAL_RECEIVED | REFUSAL_RECEIVED    |
      | REFUSAL_RECEIVED | Engagement - Contact made | Visit - Extraordinary refusal | 6-20-54      | REFUSAL_RECEIVED | REFUSAL_RECEIVED    |

      | REFUSAL_RECEIVED | Contact made              | Hard refusal                  | 7-20-02      | REFUSAL_RECEIVED | REFUSAL_RECEIVED    |
      | REFUSAL_RECEIVED | Contact made              | Extraordinary refusal         | 7-20-03      | REFUSAL_RECEIVED | REFUSAL_RECEIVED    |

      | REFUSAL_RECEIVED | Contact made              | Hard refusal                  | 8-20-02      | REFUSAL_RECEIVED | REFUSAL_RECEIVED    |
      | REFUSAL_RECEIVED | Contact made              | Extraordinary refusal         | 8-20-03      | REFUSAL_RECEIVED | REFUSAL_RECEIVED    |

      # SPG SITE ENGAGEMENT works

      | ADDRESS_NOT_VALID | Engagement - Not-valid    | Visit - Derelict or demolished | 6-10-56      | ADDRESS_NOT_VALID | ADDRESS_NOT_VALID   |
      | ADDRESS_NOT_VALID | Engagement - Not-valid    | Visit - Can't find property    | 6-10-59      | ADDRESS_NOT_VALID | ADDRESS_NOT_VALID   |
      | ADDRESS_NOT_VALID | Engagement - Not-valid    | Visit - Unaddressable object   | 6-10-61      | ADDRESS_NOT_VALID | ADDRESS_NOT_VALID   |
      | ADDRESS_NOT_VALID | Engagement - Not-valid    | Visit - Incorrect address      | 6-10-60      | ADDRESS_NOT_VALID | ADDRESS_NOT_VALID   |
      | ADDRESS_NOT_VALID | Engagement - Not-valid    | Visit - Non-residential        | 6-10-55      | ADDRESS_NOT_VALID | ADDRESS_NOT_VALID   |
      | ADDRESS_NOT_VALID | Engagement - Not-valid    | Visit - Duplicate              | 6-10-58      | ADDRESS_NOT_VALID | ADDRESS_NOT_VALID   |
      | ADDRESS_NOT_VALID | Engagement - Not-valid    | Visit - Under construction     | 6-10-57      | ADDRESS_NOT_VALID | ADDRESS_NOT_VALID   |
      | ADDRESS_NOT_VALID | Engagement - Not-valid    | Phone - Derelict or demolished | 6-10-06      | ADDRESS_NOT_VALID | ADDRESS_NOT_VALID   |
      | ADDRESS_NOT_VALID | Engagement - Not-valid    | Phone - Non-residential        | 6-10-05      | ADDRESS_NOT_VALID | ADDRESS_NOT_VALID   |
      | ADDRESS_NOT_VALID | Engagement - Not-valid    | Phone - Incorrect address      | 6-10-08      | ADDRESS_NOT_VALID | ADDRESS_NOT_VALID   |
      | ADDRESS_NOT_VALID | Engagement - Not-valid    | Phone - Under construction     | 6-10-07      | ADDRESS_NOT_VALID | ADDRESS_NOT_VALID   |
      | ADDRESS_NOT_VALID | Engagement - No contact   | Visit - Unoccupied site        | 6-30-03      | ADDRESS_NOT_VALID | ADDRESS_NOT_VALID   |
      | ADDRESS_NOT_VALID | Engagement - Contact made | Phone - Unoccupied site        | 6-20-01      | ADDRESS_NOT_VALID | ADDRESS_NOT_VALID   |
      | ADDRESS_NOT_VALID | Engagement - Contact made | Phone - Unoccupied site        | 6-20-01      | ADDRESS_NOT_VALID | ADDRESS_NOT_VALID   |
      | ADDRESS_NOT_VALID | Engagement - Contact made | Visit - Unoccupied site        | 6-20-55      | ADDRESS_NOT_VALID | ADDRESS_NOT_VALID   |

      # SPG UNIT DELIVERY works

      | ADDRESS_NOT_VALID | No contact                | Unoccupied unit                | 7-30-01      | ADDRESS_NOT_VALID | ADDRESS_NOT_VALID   |
      | ADDRESS_NOT_VALID | Not-valid                 | Derelict or demolished         | 7-10-07      | ADDRESS_NOT_VALID | ADDRESS_NOT_VALID   |
      | ADDRESS_NOT_VALID | Not-valid                 | Can't find property            | 7-10-10      | ADDRESS_NOT_VALID | ADDRESS_NOT_VALID   |
      | ADDRESS_NOT_VALID | Not-valid                 | Unaddressable object           | 7-10-06      | ADDRESS_NOT_VALID | ADDRESS_NOT_VALID   |
      | ADDRESS_NOT_VALID | Not-valid                 | Non-residential                | 7-10-05      | ADDRESS_NOT_VALID | ADDRESS_NOT_VALID   |
      | ADDRESS_NOT_VALID | Not-valid                 | Incorrect address              | 7-10-11      | ADDRESS_NOT_VALID | ADDRESS_NOT_VALID   |
      | ADDRESS_NOT_VALID | Not-valid                 | Duplicate                      | 7-10-09      | ADDRESS_NOT_VALID | ADDRESS_NOT_VALID   |
      | ADDRESS_NOT_VALID | Not-valid                 | Under construction             | 7-10-08      | ADDRESS_NOT_VALID | ADDRESS_NOT_VALID   |

      # SPG UNIT FOLLOW UP works

      | ADDRESS_NOT_VALID | No contact made           | Unoccupied unit                | 8-30-03      | ADDRESS_NOT_VALID | ADDRESS_NOT_VALID   |
      | ADDRESS_NOT_VALID | Not-valid                 | Derelict or demolished         | 8-10-07      | ADDRESS_NOT_VALID | ADDRESS_NOT_VALID   |
      | ADDRESS_NOT_VALID | Not-valid                 | Can't find property            | 8-10-10      | ADDRESS_NOT_VALID | ADDRESS_NOT_VALID   |
      | ADDRESS_NOT_VALID | Not-valid                 | Unaddressable object           | 8-10-06      | ADDRESS_NOT_VALID | ADDRESS_NOT_VALID   |
      | ADDRESS_NOT_VALID | Not-valid                 | Non-residential                | 8-10-05      | ADDRESS_NOT_VALID | ADDRESS_NOT_VALID   |
      | ADDRESS_NOT_VALID | Not-valid                 | Incorrect address              | 8-10-11      | ADDRESS_NOT_VALID | ADDRESS_NOT_VALID   |
      | ADDRESS_NOT_VALID | Not-valid                 | Duplicate                      | 8-10-09      | ADDRESS_NOT_VALID | ADDRESS_NOT_VALID   |
      | ADDRESS_NOT_VALID | Not-valid                 | Under construction             | 8-10-08      | ADDRESS_NOT_VALID | ADDRESS_NOT_VALID   |
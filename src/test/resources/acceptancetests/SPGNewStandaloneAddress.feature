@Census @Acceptance @Outcome @SPG @NewStandaloneAddress
Feature: SPG Outcome Tests

  Scenario Outline: As a Gateway I can receive a SPG outcomes of cases from TM and create Census Events
    Given the Field Officer sends a "<Type>"
    And the Primary Outcome is "<Primary Outcome>"
    And the secondary Outcome "<Secondary Outcome>"
    And Outcome code is "<Outcome Code>"
    When Gateway receives SPG New Standalone Address outcome
    Then It will send an "<Operation List>" messages to RM
    And each message conforms to "<Output Message List>"

    Examples:
      | Type                   | Primary Outcome               | Secondary Outcome                       | Outcome Code | Operation List                          | Output Message List                     |

       # LINKED QID

      | NEW_STANDALONE_ADDRESS | Access Granted - Contact made | Hard refusal                            | 10-20-02     | NEW_STANDALONE_ADDRESS,REFUSAL_RECEIVED | NEW_STANDALONE_ADDRESS,REFUSAL_RECEIVED |
      | NEW_STANDALONE_ADDRESS | Access Granted - Contact made | Extraordinary refusal                   | 10-20-03     | NEW_STANDALONE_ADDRESS,REFUSAL_RECEIVED | NEW_STANDALONE_ADDRESS,REFUSAL_RECEIVED |
      | NEW_STANDALONE_ADDRESS | Access Granted - Contact made | HICL or Paper H Questionnaire delivered | 10-20-05     | NEW_STANDALONE_ADDRESS,LINKED_QID       | NEW_STANDALONE_ADDRESS,LINKED_QID       |

      # No contact

      | NEW_STANDALONE_ADDRESS | Access Granted - No contact   | Unit unoccupied                         | 10-30-01     | NEW_STANDALONE_ADDRESS                  | NEW_STANDALONE_ADDRESS                  |
      | NEW_STANDALONE_ADDRESS | Access Granted - No contact   | Inaccessible or no access               | 10-30-02     | NEW_STANDALONE_ADDRESS                  | NEW_STANDALONE_ADDRESS                  |
      | NEW_STANDALONE_ADDRESS | Access Granted - No contact   | Resident is out                         | 10-30-03     | NEW_STANDALONE_ADDRESS                  | NEW_STANDALONE_ADDRESS                  |

      # Access not granted

      | NEW_STANDALONE_ADDRESS | Access not Granted            | Dummy information collected             | 10-40-01     | NEW_STANDALONE_ADDRESS                  | NEW_STANDALONE_ADDRESS                  |

#      FULFILMENT REQUESTED code changes required
#      | NEW_STANDALONE_ADDRESS | Access Granted - Contact made           | HUAC required by text                     | 10-20-04      | NEW_STANDALONE_ADDRESS,FULFILMENT_REQUESTED    | NEW_STANDALONE_ADDRESS,FULFILMENT_REQUESTED                      |
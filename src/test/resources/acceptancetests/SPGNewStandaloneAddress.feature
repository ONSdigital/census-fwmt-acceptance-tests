@Census @Acceptance @Outcome @SPG @NewStandaloneAddress
Feature: SPG Outcome Tests

  Scenario Outline: As a Gateway I can receive a SPG NewStandaloneAddress of cases from TM/COMET and create Census Events
    Given the Field Officer sends a "<Type>"
    And the Primary Outcome is "<Primary Outcome>"
    And the secondary Outcome "<Secondary Outcome>"
    And Outcome code is "<Outcome Code>"
    When Gateway receives SPG New Standalone Address outcome
    Then It will send an "<Operation List>" messages to RM
    And each message conforms to "<Output Message List>"

    Examples:
      | Type                   | Primary Outcome               | Secondary Outcome                       | Outcome Code | Operation List                                     | Output Message List                       |

       # LINKED QID
      | NEW_STANDALONE_ADDRESS | Access Granted - Contact made | Hard refusal                            | 10-20-02     | NEW_STANDALONE_ADDRESS,REFUSAL_RECEIVED            | NEW_ADDRESS_REPORTED,REFUSAL_RECEIVED     |
      | NEW_STANDALONE_ADDRESS | Access Granted - Contact made | Extraordinary refusal                   | 10-20-03     | NEW_STANDALONE_ADDRESS,REFUSAL_RECEIVED            | NEW_ADDRESS_REPORTED,REFUSAL_RECEIVED     |

      #  expected QUID and we got Fulful - there must be an issue with the input message
      | NEW_STANDALONE_ADDRESS | Access Granted - Contact made | HICL or Paper H Questionnaire delivered | 10-20-05     | NEW_STANDALONE_ADDRESS_LINKED,FULFILMENT_REQUESTED | NEW_ADDRESS_REPORTED,FULFILMENT_REQUESTED |

       # No contact
      | NEW_STANDALONE_ADDRESS | Access Granted - No contact   | Unit unoccupied                         | 10-30-01     | NEW_STANDALONE_ADDRESS                             | NEW_ADDRESS_REPORTED                      |
      | NEW_STANDALONE_ADDRESS | Access Granted - No contact   | Inaccessible or no access               | 10-30-02     | NEW_STANDALONE_ADDRESS                             | NEW_ADDRESS_REPORTED                      |
      | NEW_STANDALONE_ADDRESS | Access Granted - No contact   | Resident is out                         | 10-30-03     | NEW_STANDALONE_ADDRESS                             | NEW_ADDRESS_REPORTED                      |

       # Access not granted
      | NEW_STANDALONE_ADDRESS | Access not Granted            | Dummy information collected             | 10-40-01     | NEW_STANDALONE_ADDRESS                             | NEW_ADDRESS_REPORTED                      |

      # FULFILMENT REQUESTED
      | NEW_STANDALONE_ADDRESS | Access Granted - Contact made | HUAC required by text                   | 10-20-04     | NEW_STANDALONE_ADDRESS,FULFILMENT_REQUESTED        | NEW_ADDRESS_REPORTED,FULFILMENT_REQUESTED |
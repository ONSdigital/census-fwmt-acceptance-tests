@Census @Acceptance @Outcome @SPG @NewUnitAddress
Feature: SPG Outcome Tests

  Scenario Outline: As a Gateway I can receive a SPG outcomes of cases from TM and create Census Events
    Given the Field Officer sends a "<Type>"
    And the Primary Outcome is "<Primary Outcome>"
    And the secondary Outcome "<Secondary Outcome>"
    And Outcome code is "<Outcome Code>"
    When Gateway receives SPG a New Unit Address outcome
    Then It will send an "<Operation List>" messages to RM
    And each message conforms to "<Output Message List>"

    Examples:
      | Type                   | Primary Outcome               | Secondary Outcome                       | Outcome Code | Operation List                          | Output Message List                     |

      # No contact

#      | NEW_UNIT_ADDRESS | No contact   | Unit unoccupied                         | 9-20-01 | NEW_UNIT_ADDRESS                      | NEW_ADDRESS_REPORTED                      |
#      | NEW_UNIT_ADDRESS | No contact   | Inaccessible or no access               | 9-20-02 | NEW_UNIT_ADDRESS                      | NEW_ADDRESS_REPORTED                      |
#      | NEW_UNIT_ADDRESS | No contact   | Resident is out                         | 9-20-03 | NEW_UNIT_ADDRESS                      | NEW_ADDRESS_REPORTED                      |
#
#      # Contact made
#
#      | NEW_UNIT_ADDRESS | Contact made | Visit another time                      | 9-20-01 | NEW_UNIT_ADDRESS                      | NEW_ADDRESS_REPORTED                      |
#      | NEW_UNIT_ADDRESS | Contact made | Hard refusal                            | 9-20-02 | NEW_UNIT_ADDRESS,REFUSAL_RECEIVED     | NEW_ADDRESS_REPORTED,REFUSAL_RECEIVED     |
#      | NEW_UNIT_ADDRESS | Contact made | Extraordinary refusal                   | 9-20-03 | NEW_UNIT_ADDRESS,REFUSAL_RECEIVED     | NEW_ADDRESS_REPORTED,REFUSAL_RECEIVED     |


      #  Input message doesn't contain data for following - tested fuctionality in other tests
      # Fulfilment - code change
#      | NEW_UNIT_ADDRESS | Contact made | HUAC required by text                   | 9-20-04 | NEW_UNIT_ADDRESS,FULFILMENT_REQUESTED | NEW_ADDRESS_REPORTED,FULFILMENT_REQUESTED |

      # Linked Qid
      | NEW_UNIT_ADDRESS | Contact made | HICL or Paper H Questionnaire delivered | 9-20-05 | NEW_UNIT_ADDRESS_LINKED,FULFILMENT_REQUESTED           | NEW_ADDRESS_REPORTED,FULFILMENT_REQUESTED           |

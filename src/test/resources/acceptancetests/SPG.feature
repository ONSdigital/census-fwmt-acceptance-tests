@Census @Acceptance @Outcome @SPG
Feature: SPG Outcome Tests

  # think i will need a test for each type on its own to account for case id not being supplied to new unit and new standalone

  Scenario Outline: As a Gateway I can receive a SPG outcomes of cases from TM and create Census Events
    Given the Field Officer sends a "<Type>"
    And the Primary Outcome is "<Primary Outcome>"
    And the secondary Outcome "<Secondary Outcome>"
    And Outcome code is "<Outcome Code>"
    When Gateway receives SPG outcome
    Then It will send an "<Operation List>" messages to RM
    And each message conforms to "<Output Message List>"

    Examples:
      | Type              | Primary Outcome           | Secondary Outcome              | Outcome Code | Operation List    | Output Message List |

      # SPG SITE ENGAGEMENT

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

      # SPG UNIT DELIVERY

      | ADDRESS_NOT_VALID | No contact                | Unoccupied unit                | 7-30-01      | ADDRESS_NOT_VALID | ADDRESS_NOT_VALID   |

      | ADDRESS_NOT_VALID | Not-valid                 | Derelict or demolished         | 7-10-07      | ADDRESS_NOT_VALID | ADDRESS_NOT_VALID   |
      | ADDRESS_NOT_VALID | Not-valid                 | Can't find property            | 7-10-10      | ADDRESS_NOT_VALID | ADDRESS_NOT_VALID   |
      | ADDRESS_NOT_VALID | Not-valid                 | Unaddressable object           | 7-10-06      | ADDRESS_NOT_VALID | ADDRESS_NOT_VALID   |
      | ADDRESS_NOT_VALID | Not-valid                 | Non-residential                | 7-10-05      | ADDRESS_NOT_VALID | ADDRESS_NOT_VALID   |
      | ADDRESS_NOT_VALID | Not-valid                 | Incorrect address              | 7-10-11      | ADDRESS_NOT_VALID | ADDRESS_NOT_VALID   |
      | ADDRESS_NOT_VALID | Not-valid                 | Duplicate                      | 7-10-09      | ADDRESS_NOT_VALID | ADDRESS_NOT_VALID   |
      | ADDRESS_NOT_VALID | Not-valid                 | Under construction             | 7-10-08      | ADDRESS_NOT_VALID | ADDRESS_NOT_VALID   |

      # SPG UNIT FOLLOW UP

      | ADDRESS_NOT_VALID | No contact made           | Unoccupied unit                | 8-30-03      | ADDRESS_NOT_VALID | ADDRESS_NOT_VALID   |

      | ADDRESS_NOT_VALID | Not-valid                 | Derelict or demolished         | 8-10-07      | ADDRESS_NOT_VALID | ADDRESS_NOT_VALID   |
      | ADDRESS_NOT_VALID | Not-valid                 | Can't find property            | 8-10-10      | ADDRESS_NOT_VALID | ADDRESS_NOT_VALID   |
      | ADDRESS_NOT_VALID | Not-valid                 | Unaddressable object           | 8-10-06      | ADDRESS_NOT_VALID | ADDRESS_NOT_VALID   |
      | ADDRESS_NOT_VALID | Not-valid                 | Non-residential                | 8-10-05      | ADDRESS_NOT_VALID | ADDRESS_NOT_VALID   |
      | ADDRESS_NOT_VALID | Not-valid                 | Incorrect address              | 8-10-11      | ADDRESS_NOT_VALID | ADDRESS_NOT_VALID   |
      | ADDRESS_NOT_VALID | Not-valid                 | Duplicate                      | 8-10-09      | ADDRESS_NOT_VALID | ADDRESS_NOT_VALID   |
      | ADDRESS_NOT_VALID | Not-valid                 | Under construction             | 8-10-08      | ADDRESS_NOT_VALID | ADDRESS_NOT_VALID   |

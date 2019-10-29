@Census @Acceptance @Outcome @CCSHouseholdPL
Feature: CCS Interview Outcome Tests

  Scenario Outline: As a Gateway I can receive a CCS interview outcome of cases from TM and create Census Events
    Given TM sends a "<InputMessage>" Census Case CCS interview Outcome to the Gateway with "<Primary Outcome>" and with the category "<Category>"
    And the Primary Outcome for CCS interview is "<Primary Outcome>"
    And the Secondary Outcome for CCS interview is "<InputMessage>"
    When the Outcome Service processes the CCS interview message
    Then the Outcome Service for the CCS interview should create a valid "<CaseEvent>"
    And and of the correct CCS interview "<EventType>"

    Examples:
      | InputMessage                      | Primary Outcome | Category | CaseEvent      | EventType            |
      | Left questionnaire on final visit | Contact made    | HH       | Field.other    | QUESTIONNAIRE_LINKED |
      | Left questionnaire on final visit | No contact      | HH       | Field.other    | QUESTIONNAIRE_LINKED |
      | Complete on paper (full)          | Contact made    | CE       | Field.other    | QUESTIONNAIRE_LINKED |
      | Complete on paper (full)          | Contact made    | HH       | Field.other    | QUESTIONNAIRE_LINKED |
      | Complete on paper (partial)       | Contact made    | CE       | Field.other    | QUESTIONNAIRE_LINKED |
      | Complete on paper (partial)       | Contact made    | HH       | Field.other    | QUESTIONNAIRE_LINKED |
      | Hard refusal                      | Contact made    | CE       | Field.refusals | REFUSAL_RECEIVED     |
      | Hard refusal                      | Contact made    | HH       | Field.refusals | REFUSAL_RECEIVED     |
      | Extraordinary refusal             | Contact made    | CE       | Field.refusals | REFUSAL_RECEIVED     |
      | Extraordinary refusal             | Contact made    | HH       | Field.refusals | REFUSAL_RECEIVED     |
      | Property not in postcode boundary | Contact made    | HH       | Field.other    | ADDRESS_NOT_VALID    |
      | Split address                     | Contact made    | HH       | Field.other    | ADDRESS_NOT_VALID    |
      | Property is a CE                  | Not Valid       | HH       | Field.other    | ADDRESS_NOT_VALID    |
      | Derelict                          | Not Valid       | CE       | Field.other    | ADDRESS_NOT_VALID    |
      | Derelict                          | Not Valid       | HH       | Field.other    | ADDRESS_NOT_VALID    |
      | Non residential or business       | Not Valid       | CE       | Field.other    | ADDRESS_NOT_VALID    |
      | Non residential or business       | Not Valid       | HH       | Field.other    | ADDRESS_NOT_VALID    |
      | Demolished                        | Not Valid       | CE       | Field.other    | ADDRESS_NOT_VALID    |
      | Demolished                        | Not Valid       | HH       | Field.other    | ADDRESS_NOT_VALID    |
      | Duplicate                         | Not Valid       | CE       | Field.other    | ADDRESS_NOT_VALID    |
      | Duplicate                         | Not Valid       | HH       | Field.other    | ADDRESS_NOT_VALID    |
      | Under construction                | Not Valid       | CE       | Field.other    | ADDRESS_NOT_VALID    |
      | Under construction                | Not Valid       | HH       | Field.other    | ADDRESS_NOT_VALID    |
      | Incorrect address                 | Not Valid       | CE       | Field.other    | ADDRESS_NOT_VALID    |
      | Incorrect address                 | Not Valid       | HH       | Field.other    | ADDRESS_NOT_VALID    |
      | Property is a household           | Not Valid       | CE       | Field.other    | ADDRESS_NOT_VALID    |
      | CE out of scope                   | Contact made    | CE       | Field.other    | ADDRESS_NOT_VALID    |

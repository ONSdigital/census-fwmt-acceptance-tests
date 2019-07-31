@Census @Acceptance @Outcome @CCSHouseholdPL
Feature: Non-valid household Outcome Tests

  Scenario Outline: As a Gateway I can receive a CCS interview outcome of cases from TM and create Census Events
    Given TM sends a "<InputMessage>" Census Case CCS interview Outcome to the Gateway with "<Primary Outcome>" and with the category "<Category>"
    And the Primary Outcome for CCS interview is "<Primary Outcome>"
    And the Secondary Outcome for CCS interview is "<InputMessage>"
    When the Outcome Service processes the CCS interview message
    Then the Outcome Service for the CCS interview should create a valid "<CaseEvent>" for the correct "<Topic>"
    And and of the correct CCS interview "<EventType>"

    Examples:
      | InputMessage                      | Primary Outcome | Category | CaseEvent                    | Topic                      | EventType            |
      | Left questionnaire on final visit | Contact made    | HH       | Gateway.Questionnaire.Update | event.questionnaire.update | QUESTIONNAIRE_LINKED |
      | Left questionnaire on final visit | No contact      | HH       | Gateway.Questionnaire.Update | event.questionnaire.update | QUESTIONNAIRE_LINKED |
      | Complete on paper (full)          | Contact made    | CE       | Gateway.Questionnaire.Update | event.questionnaire.update | QUESTIONNAIRE_LINKED |
      | Complete on paper (full)          | Contact made    | HH       | Gateway.Questionnaire.Update | event.questionnaire.update | QUESTIONNAIRE_LINKED |
      | Complete on paper (partial)       | Contact made    | CE       | Gateway.Questionnaire.Update | event.questionnaire.update | QUESTIONNAIRE_LINKED |
      | Complete on paper (partial)       | Contact made    | HH       | Gateway.Questionnaire.Update | event.questionnaire.update | QUESTIONNAIRE_LINKED |
      | Hard refusal                      | Contact made    | CE       | Gateway.Respondent.Refusal   | event.respondent.refusal   | REFUSAL_RECEIVED     |
      | Hard refusal                      | Contact made    | HH       | Gateway.Respondent.Refusal   | event.respondent.refusal   | REFUSAL_RECEIVED     |
      | Extraordinary refusal             | Contact made    | CE       | Gateway.Respondent.Refusal   | event.respondent.refusal   | REFUSAL_RECEIVED     |
      | Extraordinary refusal             | Contact made    | HH       | Gateway.Respondent.Refusal   | event.respondent.refusal   | REFUSAL_RECEIVED     |
      | Property not in postcode boundary | Contact made    | HH       | Gateway.Address.Update       | event.case.address.update  | ADDRESS_NOT_VALID    |
      | Split address                     | Contact made    | HH       | Gateway.Address.Update       | event.case.address.update  | ADDRESS_NOT_VALID    |
      | Property is a CE                  | Not valid       | HH       | Gateway.Address.Update       | event.case.address.update  | ADDRESS_NOT_VALID    |
      | Derelict                          | Not valid       | CE       | Gateway.Address.Update       | event.case.address.update  | ADDRESS_NOT_VALID    |
      | Derelict                          | Not valid       | HH       | Gateway.Address.Update       | event.case.address.update  | ADDRESS_NOT_VALID    |
      | Non residential or business       | Not valid       | CE       | Gateway.Address.Update       | event.case.address.update  | ADDRESS_NOT_VALID    |
      | Non residential or business       | Not valid       | HH       | Gateway.Address.Update       | event.case.address.update  | ADDRESS_NOT_VALID    |
      | Demolished                        | Not valid       | CE       | Gateway.Address.Update       | event.case.address.update  | ADDRESS_NOT_VALID    |
      | Demolished                        | Not valid       | HH       | Gateway.Address.Update       | event.case.address.update  | ADDRESS_NOT_VALID    |
      | Duplicate                         | Not valid       | CE       | Gateway.Address.Update       | event.case.address.update  | ADDRESS_NOT_VALID    |
      | Duplicate                         | Not valid       | HH       | Gateway.Address.Update       | event.case.address.update  | ADDRESS_NOT_VALID    |
      | Under construction                | Not valid       | CE       | Gateway.Address.Update       | event.case.address.update  | ADDRESS_NOT_VALID    |
      | Under construction                | Not valid       | HH       | Gateway.Address.Update       | event.case.address.update  | ADDRESS_NOT_VALID    |
      | Incorrect address                 | Not valid       | CE       | Gateway.Address.Update       | event.case.address.update  | ADDRESS_NOT_VALID    |
      | Incorrect address                 | Not valid       | HH       | Gateway.Address.Update       | event.case.address.update  | ADDRESS_NOT_VALID    |
      | Property is a household           | Not valid       | CE       | Gateway.Address.Update       | event.case.address.update  | ADDRESS_NOT_VALID    |
      | CE out of scope                   | Contact made    | CE       | Gateway.Address.Update       | event.case.address.update  | ADDRESS_NOT_VALID    |

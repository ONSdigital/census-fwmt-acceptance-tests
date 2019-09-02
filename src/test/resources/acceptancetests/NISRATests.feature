@Census @Acceptance
Feature: NISRA Tests

  Scenario: As Gateway I can receive NISRA Household create job requests from RM
    Given RM sends a create NISRA job request job which has a case ID of "b3815f29-5299-48fe-be1d-ef81ee301f59" and a field officer ID "allocatedOfficer"
    And the Gateway sends a create NISRA Job message to TM with case ID of "b3815f29-5299-48fe-be1d-ef81ee301f59"
    Then a new case with id of "b3815f29-5299-48fe-be1d-ef81ee301f59" is created in TM for NISRA

  Scenario: As Gateway I cannot receive NISRA Household create job requests from RM without a field officer ID
    Given RM sends a create NISRA job request job which has a case ID of "39bad71c-7de5-4e1b-9a07-d9597737977f"
    Then RM will throw an exception for case ID "39bad71c-7de5-4e1b-9a07-d9597737977f" for NISRA
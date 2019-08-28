@Census @Acceptance
Feature: CCS Interview Tests

  Scenario: As Gateway I can receive a CCS Interview create job requests from RM
    Given TM doesn't have an existing job CCS Interview with case ID "e6e3e714-2f26-4909-a564-b8d4d0c8ba49"
    And TM sends a CCS PL Outcome to the gateway with case ID "e6e3e714-2f26-4909-a564-b8d4d0c8ba49"
    And the Outcome Service processes the CCS PL message and sends to RM
    When RM sends a create CCS Interview job request
    When the Gateway sends a Create CCS Interview Job message to TM
    Then a new CCS Interview case with id of "e6e3e714-2f26-4909-a564-b8d4d0c8ba49" is created in TM
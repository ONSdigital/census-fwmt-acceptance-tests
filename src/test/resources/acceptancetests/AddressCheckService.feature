@Census @Acceptance
Feature: Address Check CSV service Tests

  Scenario: As a Gateway I can receive a CSV file for Address Check and can pass the content to TM
    Given the Gateway receives a CSV Address Check
    Then a new case id for job containing postcode "E4 7NG" is created in TM
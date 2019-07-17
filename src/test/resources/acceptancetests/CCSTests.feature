@Census @Acceptance
Feature: CCS Tests

  Scenario: As a Gateway I can receive a CSV file for a CCS and can pass the content to TM
    Given the Gateway receives a CSV CCS
    Then a new case with new case id for job containing postcode "E4 7NG" is created in TM
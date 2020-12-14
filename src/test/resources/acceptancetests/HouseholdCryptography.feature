@Census @Acceptance @Cryptography @Outbound @Inbound
Feature: Household Cryptography Tests

  Scenario: As Gateway I can receive a hard refusal from TM and encyrpt the house holder names
    Given TM doesnt have a job with case ID "bd6345af-d706-43d3-a13b-8c549e081a76" in TM
    When the gateway receives a hard refusal for a household from TM with caseId "bd6345af-d706-43d3-a13b-8c549e081a76"
    Then the gateway will encrypt the name of the householder
    Then the gateway will send the case to RM

  Scenario: As Gateway I can decrypt a householders details when a create NC is received from RM
    Given TM doesnt have a job with case ID "bd6345af-d706-43d3-a13b-8c549e081a76" in TM
    When gateway receives a NC create from RM with case ID "bd6345af-d706-43d3-a13b-8c549e081a76"
    Then the gateway will retrieve and decrypt the householders name from the RM Case API for case ID "bd6345af-d706-43d3-a13b-8c549e081a76"
    Then the gateway will send the case to TM
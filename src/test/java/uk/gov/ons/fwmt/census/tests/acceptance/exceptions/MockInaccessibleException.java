package uk.gov.ons.fwmt.census.tests.acceptance.exceptions;

public class MockInaccessibleException extends RuntimeException {
  public MockInaccessibleException(String reason) {
    super(reason);
  }
}

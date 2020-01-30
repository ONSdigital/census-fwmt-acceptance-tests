package uk.gov.ons.census.fwmt.tests.acceptance.utils;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

@Slf4j
@Component
public final class QueueClient {

  @Autowired
  private QueueUtils queueUtils;

  public long getMessageCount(String queueName) {
    Long messageCount = queueUtils.getMessageCount(queueName);
    return messageCount;
  }

  public String getMessage(String queueName) throws InterruptedException {
    return getMessage(queueName, 10000, 10);
  }

  public String getMessage(String queueName, int msTimeout) throws InterruptedException {
    return getMessage(queueName, msTimeout, 10);
  }

  public String getMessage(String queueName, int msTimeout, int msInterval) throws InterruptedException {
    String message = null;
    int iterations = (msTimeout + msInterval - 1) / msInterval; // division rounding up
    for (int i = 0; i < iterations; i++) {
      message = queueUtils.getMessageOffQueue(queueName);
      if (message != null) {
        break;
      }
      Thread.sleep(msInterval);
    }
    return message;
  }

  public void sendToRMFieldQueue(String message) {
    String exchangeName = "";
    String routingKey = "RM.Field";
    String virtualHost = "/rm";
    queueUtils.addMessage(virtualHost, exchangeName, routingKey, message);
  }

  public void clearQueues() {
    clearQueue("Gateway.Actions", "/");
    clearQueue("Gateway.ActionsDLQ", "/");
    clearQueue("Outcome.Preprocessing", "/");
    clearQueue("Outcome.PreprocessingDLQ", "/");

    clearQueue("RM.Field", "/rm");
    clearQueue("RM.FieldDLQ", "/rm");
    clearQueue("Field.other", "/rm");
    clearQueue("Field.refusals", "/rm");
  }

  public void createQueue() throws IOException, TimeoutException {
    queueUtils.createOutcomeQueues();
  }

  private void clearQueue(String queueName, String virtualHost) {
    queueUtils.deleteMessage(queueName, virtualHost);
  }
}

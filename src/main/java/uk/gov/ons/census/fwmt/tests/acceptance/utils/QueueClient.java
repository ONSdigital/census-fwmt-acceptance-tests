package uk.gov.ons.census.fwmt.tests.acceptance.utils;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.concurrent.TimeoutException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

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

    public void sendToRMFieldQueue(String message, String type) throws URISyntaxException {
        String exchangeName = "";
        String routingKey = "RM.Field";
        queueUtils.addMessage(exchangeName, routingKey, message, type);
    }

    public void clearQueues() throws URISyntaxException {
        clearQueue("RM.Field");
        clearQueue("RM.FieldDLQ");
        clearQueue("Outcome.Preprocessing");
        clearQueue("Outcome.PreprocessingDLQ");
        clearQueue("Field.other");
        clearQueue("Field.refusals");
    }

    public void createQueue() throws IOException, TimeoutException, InterruptedException {
      queueUtils.createOutcomeQueues();
    }
    private void clearQueue(String queueName) throws URISyntaxException {
       queueUtils.deleteMessage(queueName);
    }

}

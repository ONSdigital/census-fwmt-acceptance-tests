package uk.gov.ons.census.fwmt.tests.acceptance.utils;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.concurrent.TimeoutException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;
import uk.gov.ons.census.fwmt.events.utils.GatewayEventMonitor;

@Slf4j
@Component
public final class QueueClient {

  private static final String FIELD_REFUSALS_QUEUE = "Field.refusals";

  private static final String TEMP_FIELD_OTHERS_QUEUE = "Field.other";

  private static final String RM_FIELD_QUEUE = "RM.Field";

  private static final String RM_FIELD_QUEUE_DLQ = "RM.FieldDLQ";

  private static final String OUTCOME_PRE_PROCESSING = "Outcome.Preprocessing";

  private static final String OUTCOME_PRE_PROCESSING_DLQ = "Outcome.PreprocessingDLQ";

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

    public void clearQueues(String... qnames) throws URISyntaxException {
      for (String q : qnames) {
        clearQueue(q);
      }
    }

    public void createQueue() throws IOException, TimeoutException, InterruptedException {
      queueUtils.createOutcomeQueues();
    }
    private void clearQueue(String queueName) throws URISyntaxException {
       queueUtils.deleteMessage(queueName);
    }

    public void reset() throws Exception {
      clearQueues(FIELD_REFUSALS_QUEUE, TEMP_FIELD_OTHERS_QUEUE, RM_FIELD_QUEUE, RM_FIELD_QUEUE_DLQ, OUTCOME_PRE_PROCESSING,
          OUTCOME_PRE_PROCESSING_DLQ);
    }

}
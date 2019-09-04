package uk.gov.ons.census.fwmt.tests.acceptance.utils;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.net.URISyntaxException;

@Slf4j
@Component
public final class QueueUtils {
    @Value("${service.mocktm.url}")
    private String mockTmURL;

    private RestTemplate restTemplate = new RestTemplate();

    public long getMessageCount(String queueName) {
        RestTemplate restTemplate = new RestTemplate();
        String messageCountUrl = mockTmURL + "/queue/count/" + queueName;
        ResponseEntity<Long> messageCount = restTemplate.getForEntity(messageCountUrl, Long.class);
        return messageCount.getBody();
    }

    public String getMessage(String queueName) throws InterruptedException {
        return getMessage(queueName, 10000, 10);
    }

    public String getMessage(String queueName, int msTimeout) throws InterruptedException {
        return getMessage(queueName, msTimeout, 10);
    }

    public String getMessage(String queueName, int msTimeout, int msInterval) throws InterruptedException {
        RestTemplate restTemplate = new RestTemplate();
        String messageUrl = mockTmURL + "/queue/message/?qname=" + queueName;
        String message = null;
        int iterations = (msTimeout + msInterval - 1) / msInterval; // division rounding up
        for (int i = 0; i < iterations; i++) {
            ResponseEntity<String> messageEntity = restTemplate.getForEntity(messageUrl, String.class);
            message = messageEntity.getBody();
            if (message != null) {
                break;
            }
            Thread.sleep(msInterval);
        }
        return message;
    }

    public ResponseEntity<String> getMessageOffQueueWithRoutingKey(String queueName, String routingKey)
            throws InterruptedException {
        return getMessageOffQueueWithRoutingKey(queueName, routingKey, 10000, 1000);
    }

    public ResponseEntity<String> getMessageOffQueueWithRoutingKey(String queueName, String routingKey, int msTimeout)
            throws InterruptedException {
        return getMessageOffQueueWithRoutingKey(queueName, routingKey, msTimeout, 1000);
    }

    public ResponseEntity<String> getMessageOffQueueWithRoutingKey(String queueName, String routingKey, int msTimeout, int msInterval)
            throws InterruptedException {
        RestTemplate restTemplate = new RestTemplate();
        String messageUrl = mockTmURL + "/queue/messagewithrouting/?qname=" + queueName + "&routingKey=" + routingKey;
        ResponseEntity<String> messageEntity = null;
        int iterations = (msTimeout + msInterval - 1) / msInterval; // division rounding up
        for (int i = 0; i < iterations; i++) {
            try {
                messageEntity = restTemplate.getForEntity(messageUrl, String.class);
            } catch (RestClientException e) {
                break;
            }
            Thread.sleep(msInterval);
        }
        return messageEntity;
    }

    public void sendToRMFieldQueue(String message) throws URISyntaxException {
        String exchangeName = "";
        String routingKey = "RM.Field";
        RestTemplate rt = new RestTemplate();
        HttpEntity<String> httpEntity = new HttpEntity<>(message);
        URI uri = new URI(mockTmURL + "/queue/?exchange=" + exchangeName + "&routingkey=" + routingKey);
        rt.postForLocation(uri, httpEntity);
    }

    public void clearQueues() throws URISyntaxException {
        clearQueue("Field.other");
        clearQueue("Field.refusals");
        clearQueue("Gateway.Actions");
        clearQueue("Gateway.ActionsDLQ");
        clearQueue("RM.Field");
        clearQueue("RM.FieldDLQ");
        clearQueue("Outcome.Preprocessing");
        clearQueue("Outcome.PreprocessingDLQ");
        clearQueue("Field.other");
        clearQueue("Field.refusals");
    }

    private void clearQueue(String queueName) throws URISyntaxException {
        URI uri = new URI(mockTmURL + "/queue/?qname=" + queueName);
        restTemplate.delete(uri);
    }

}

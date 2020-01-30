package uk.gov.ons.census.fwmt.tests.acceptance.utils;

import com.rabbitmq.client.AMQP.BasicProperties;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.GetResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

@Slf4j
@Component
public class QueueUtils {

  // Queue names
  public static final String FIELD_REFUSALS_QUEUE = "Field.refusals";
  public static final String TEMP_FIELD_OTHERS_QUEUE = "Field.other";

  // Exchange name
  public static final String GATEWAY_OUTCOME_EXCHANGE = "events";

  // Routing keys
  // keys mentioned by Dave Mort
  public static final String GATEWAY_RESPONDENT_REFUSAL_ROUTING_KEY = "event.respondent.refusal";
  public static final String GATEWAY_ADDRESS_UPDATE_ROUTING_KEY = "event.case.address.update";
  public static final String GATEWAY_FULFILMENT_REQUEST_ROUTING_KEY = "event.fulfilment.request";
  public static final String GATEWAY_QUESTIONNAIRE_UPDATE_ROUTING_KEY = "event.questionnaire.update";
  public static final String GATEWAY_CCS_PROPERTYLISTING_ROUTING_KEY = "event.ccs.propertylisting";
  public static final String GATEWAY_EVENT_FIELDCASE_UPDATE_ROUTING_KEY = "event.fieldcase.update";
  public static final String GATEWAY_CASE_APPOINTMENT_ROUTING_KEY = "event.case.appointment";
  public static final String GATEWAY_RESPONSE_AUTHENTICATION_ROUTING_KEY = "event.response.authentication";
  // keys unused but in RM data dictionary
  public static final String GATEWAY_FULFILMENT_CONFIRMED_ROUTING_KEY = "event.fulfilment.confirmed";
  public static final String GATEWAY_RESPONSE_RECEIPT_ROUTING_KEY = "event.response.receipt";
  public static final String GATEWAY_UAC_UPDATED_ROUTING_KEY = "event.uac.update";
  public static final String GATEWAY_CASE_UPDATE_ROUTING_KEY = "event.case.update";
  public static final String GATEWAY_SAMPLEUNIT_UPDATE_ROUTING_KEY = "event.sampleunit.update";

  @Value("${service.rabbit.url}")
  private String rabbitmqHost;

  @Value("${service.rabbit.username}")
  private String rabbitmqUsername;

  @Value("${service.rabbit.password}")
  private String rabbitmqPassword;

  @Value("${service.rabbit.port:5672}")
  private int rabbitmqPort;

  @Value("${service.rabbit.virtualHost:/rm}")
  private String rabbitmqRmVirtualHost;

  @Value("${service.rabbit.virtualHost:/}")
  private String rabbitmqVirtualHost;

  public String getMessageOffQueue(String qname) {
    Connection connection = null;
    Channel channel = null;
    try {
      ConnectionFactory factory = createConnectionFactory(rabbitmqHost, rabbitmqUsername, rabbitmqPassword,
          rabbitmqRmVirtualHost);
      connection = factory.newConnection();
      channel = connection.createChannel();

      GetResponse response = channel.basicGet(qname, true);
      if (response == null) {
        return null;
      } else {
        byte[] body = response.getBody();
        log.info("recieved msg from Queue: " + qname);
        return new String(body);
      }
    } catch (IOException | TimeoutException e) {
      throw new RuntimeException("Message not found", e);
    } finally {
      try {
        if (channel != null)
          channel.close();
        if (connection != null)
          connection.close();
      } catch (Exception e) {
        log.error("Issue closing RabbitMQ connections", e);
      }
    }
  }

  public Long getMessageCount(String qname) {
    Connection connection = null;
    Channel channel = null;
    try {
      ConnectionFactory factory = getRabbitMQConnectionFactory();
      connection = factory.newConnection();
      channel = connection.createChannel();

      long messageCount = channel.messageCount(qname);
      log.info("recieved msg count from Queue: " + qname);
      return messageCount;
    } catch (IOException | TimeoutException e) {
      log.error("Issue getting message count from {} queue.", qname, e);
      throw new RuntimeException("problem getting count from q", e);
    } finally {
      try {
        if (channel != null)
          channel.close();
        if (connection != null)
          connection.close();
      } catch (Exception e) {
        log.error("Issue closing RabbitMQ connections", e);
      }
    }
  }

  public Boolean addMessage(String virtualHost, String exchange, String routingKey, String message) {
    Connection connection = null;
    Channel channel = null;
    try {
      ConnectionFactory factory = createConnectionFactory(rabbitmqHost, rabbitmqUsername, rabbitmqPassword,
          virtualHost);
      connection = factory.newConnection();
      channel = connection.createChannel();

      BasicProperties.Builder builder = new BasicProperties.Builder();
      builder.contentType("text/xml");
      BasicProperties properties = builder.build();

      channel.basicPublish(exchange, routingKey, properties, message.getBytes());
      log.info("Published to exchange: " + exchange);

      return true;
    } catch (IOException | TimeoutException e) {
      log.error("Issue adding message to {} exchange.", e, exchange);
      return false;
    } finally {
      try {
        if (channel != null)
          channel.close();
        if (connection != null)
          connection.close();
      } catch (IOException | TimeoutException e) {
        log.error("Issue closing RabbitMQ connections", e);
      }
    }
  }

  public Boolean deleteMessage(String qname, String virtualHost) {
    Connection connection = null;
    Channel channel = null;
    try {
      ConnectionFactory factory = createConnectionFactory(rabbitmqHost, rabbitmqUsername, rabbitmqPassword,
          virtualHost);
      connection = factory.newConnection();
      channel = connection.createChannel();

      channel.queuePurge(qname);
      log.info("Purged Queue: " + qname);

      return true;
    } catch (IOException | TimeoutException e) {
      log.error("Issue deleting message from {} queue.", e, qname);
      return false;
    } finally {
      try {
        if (channel != null)
          channel.close();
        if (connection != null)
          connection.close();
      } catch (IOException | TimeoutException e) {
        log.error("Issue closing RabbitMQ connections", e);
      }
    }
  }

  public void createOutcomeQueues() throws IOException, TimeoutException {
    Connection connection;
    Channel channel;

    ConnectionFactory factory = createConnectionFactory(rabbitmqHost, rabbitmqUsername, rabbitmqPassword,
        rabbitmqRmVirtualHost);
    connection = factory.newConnection();
    channel = connection.createChannel();

    channel.exchangeDeclare(GATEWAY_OUTCOME_EXCHANGE, "topic");

    channel.queueDeclare(FIELD_REFUSALS_QUEUE, false, false, true, null);
    channel.queueDeclare(TEMP_FIELD_OTHERS_QUEUE, false, false, true, null);

    channel.queueBind(FIELD_REFUSALS_QUEUE, GATEWAY_OUTCOME_EXCHANGE, GATEWAY_RESPONDENT_REFUSAL_ROUTING_KEY);

    channel.queueBind(TEMP_FIELD_OTHERS_QUEUE, GATEWAY_OUTCOME_EXCHANGE, GATEWAY_ADDRESS_UPDATE_ROUTING_KEY);
    channel.queueBind(TEMP_FIELD_OTHERS_QUEUE, GATEWAY_OUTCOME_EXCHANGE, GATEWAY_FULFILMENT_REQUEST_ROUTING_KEY);
    channel.queueBind(TEMP_FIELD_OTHERS_QUEUE, GATEWAY_OUTCOME_EXCHANGE, GATEWAY_QUESTIONNAIRE_UPDATE_ROUTING_KEY);
    channel.queueBind(TEMP_FIELD_OTHERS_QUEUE, GATEWAY_OUTCOME_EXCHANGE, GATEWAY_CCS_PROPERTYLISTING_ROUTING_KEY);
    channel.queueBind(TEMP_FIELD_OTHERS_QUEUE, GATEWAY_OUTCOME_EXCHANGE, GATEWAY_EVENT_FIELDCASE_UPDATE_ROUTING_KEY);
    channel.queueBind(TEMP_FIELD_OTHERS_QUEUE, GATEWAY_OUTCOME_EXCHANGE, GATEWAY_CASE_APPOINTMENT_ROUTING_KEY);
    channel.queueBind(TEMP_FIELD_OTHERS_QUEUE, GATEWAY_OUTCOME_EXCHANGE, GATEWAY_RESPONSE_AUTHENTICATION_ROUTING_KEY);

    channel.queueBind(TEMP_FIELD_OTHERS_QUEUE, GATEWAY_OUTCOME_EXCHANGE, GATEWAY_FULFILMENT_CONFIRMED_ROUTING_KEY);
    channel.queueBind(TEMP_FIELD_OTHERS_QUEUE, GATEWAY_OUTCOME_EXCHANGE, GATEWAY_RESPONSE_RECEIPT_ROUTING_KEY);
    channel.queueBind(TEMP_FIELD_OTHERS_QUEUE, GATEWAY_OUTCOME_EXCHANGE, GATEWAY_UAC_UPDATED_ROUTING_KEY);
    channel.queueBind(TEMP_FIELD_OTHERS_QUEUE, GATEWAY_OUTCOME_EXCHANGE, GATEWAY_CASE_UPDATE_ROUTING_KEY);
    channel.queueBind(TEMP_FIELD_OTHERS_QUEUE, GATEWAY_OUTCOME_EXCHANGE, GATEWAY_SAMPLEUNIT_UPDATE_ROUTING_KEY);
  }

  private ConnectionFactory createConnectionFactory(String host, String username, String password,
      String virtualHost) {
    ConnectionFactory factory = new ConnectionFactory();
    factory.setHost(host);
    factory.setUsername(username);
    factory.setPassword(password);
    factory.setVirtualHost(virtualHost);
    return factory;
  }

  private ConnectionFactory getRabbitMQConnectionFactory() {
    ConnectionFactory factory = new ConnectionFactory();
    factory.setHost(rabbitmqHost);
    factory.setUsername(rabbitmqUsername);
    factory.setPassword(rabbitmqPassword);
    factory.setVirtualHost(rabbitmqVirtualHost);
    return factory;
  }
}

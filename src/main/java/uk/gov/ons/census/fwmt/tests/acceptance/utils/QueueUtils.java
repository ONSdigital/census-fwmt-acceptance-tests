package uk.gov.ons.census.fwmt.tests.acceptance.utils;

import com.rabbitmq.client.AMQP.BasicProperties;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.GetResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import uk.gov.ons.census.fwmt.tests.acceptance.utils.NodeCheck.NodeCheckBuilder;

import java.io.IOException;
import java.time.Instant;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.TimeoutException;

@Slf4j
@Component
public class QueueUtils {

  @Value("${service.rabbit.rm.url}")
  private String rabbitmqHost;

  @Value("${service.rabbit.rm.username}")
  private String rabbitmqUsername;

  @Value("${service.rabbit.rm.password}")
  private String rabbitmqPassword;

  @Value("${service.rabbit.rm.port:5672}")
  private int rabbitmqPort;

  @Value("${service.rabbit.rm.virtualHost:/}")
  private String rabbitmqVirtualHost;


  //gw
  @Value("${service.rabbit.gw.url}")
  private String rabbitmqGWHost;

  @Value("${service.rabbit.gw.username}")
  private String rabbitmqGWUsername;

  @Value("${service.rabbit.gw.password}")
  private String rabbitmqGWPassword;

  @Value("${service.rabbit.gw.port:5673}")
  private int rabbitmqGWPort;

  @Value("${service.rabbit.gw.virtualHost:/}")
  private String rabbitmqGWVirtualHost;

  // Queue names
  public static final String FIELD_REFUSALS_QUEUE = "Field.refusals";
  public static final String TEMP_FIELD_OTHERS_QUEUE = "Field.other";

  // Exchange name
  public static final String GATEWAY_OUTCOME_EXCHANGE = "events";

  public static final String GATEWAY_ADDRESS_UPDATE_ROUTING_KEY = "event.case.address.update";
  public static final String GATEWAY_CCS_PROPERTY_LISTING_ROUTING_KEY = "event.ccs.propertylisting";
  public static final String GATEWAY_EVENT_FIELDCASE_UPDATE_ROUTING_KEY = "event.fieldcase.update";
  public static final String GATEWAY_FULFILMENT_REQUEST_ROUTING_KEY = "event.fulfilment.request";
  public static final String GATEWAY_QUESTIONNAIRE_UPDATE_ROUTING_KEY = "event.questionnaire.update";
  public static final String GATEWAY_RESPONDENT_REFUSAL_ROUTING_KEY = "event.respondent.refusal";

  public String getMessageOffQueue(String qname) {
    Connection connection = null;
    Channel channel = null;
    try {
      ConnectionFactory factory = getRabbitMQConnectionFactory();
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

  private ConnectionFactory getRabbitMQConnectionFactory() {
    ConnectionFactory factory = new ConnectionFactory();
    factory.setHost(rabbitmqHost);
    factory.setUsername(rabbitmqUsername);
    factory.setPassword(rabbitmqPassword);
    factory.setVirtualHost(rabbitmqVirtualHost);
    factory.setPort(rabbitmqPort);
    return factory;
  }

  private ConnectionFactory getRabbitMQGWConnectionFactory() {
    ConnectionFactory factory = new ConnectionFactory();
    factory.setHost(rabbitmqGWHost);
    factory.setUsername(rabbitmqGWUsername);
    factory.setPassword(rabbitmqGWPassword);
    factory.setVirtualHost(rabbitmqGWVirtualHost);
    factory.setPort(rabbitmqGWPort);
    return factory;
  }

  public void addMessage(String exchange, String routingkey, String message, String type)  {
    Connection connection = null;
    Channel channel = null;
    try {
      ConnectionFactory factory = getRabbitMQConnectionFactory();
      connection = factory.newConnection();
      channel = connection.createChannel();

      BasicProperties.Builder builder = new BasicProperties.Builder();
      if (type.equals("create")) {
        builder.headers(Map.of("__TypeId__", "uk.gov.ons.census.fwmt.common.rm.dto.FwmtActionInstruction"));
      } else if (type.equals("cancel")) {
        builder.headers(Map.of("__TypeId__", "uk.gov.ons.census.fwmt.common.rm.dto.FwmtCancelActionInstruction"));
      } else if (type.equals("update")) {
        builder.headers(Map.of("__TypeId__", "uk.gov.ons.census.fwmt.common.rm.dto.FwmtActionInstruction"));
      }
      builder.contentType("application/json");
      builder.contentEncoding("UTF-8");
      Date date = new Date(Instant.now().toEpochMilli());
      builder.timestamp(date);
      try {
        Thread.sleep(1000);
      } catch (InterruptedException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }
      System.out.println("Date::" + date.toInstant().toEpochMilli());
      BasicProperties properties = builder.build();

      channel.basicPublish(exchange, routingkey, properties, message.getBytes());
      log.info("Published to exchange: " + exchange);

    } catch (IOException | TimeoutException e) {
      log.error("Issue adding message to {} exchange.", e, exchange);
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

  public void deleteRMMessage(String qname) {
    Connection connection = null;
    Channel channel = null;
    try {
      ConnectionFactory factory = getRabbitMQConnectionFactory();
      connection = factory.newConnection();
      channel = connection.createChannel();

      channel.queuePurge(qname);
      log.info("Purged Queue: " + qname);

    } catch (IOException | TimeoutException e) {
      log.error("Issue deleting message from {} queue.", e, qname);
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

  public void deleteGWMessage(String qname) {
    Connection connection = null;
    Channel channel = null;
    try {
      ConnectionFactory factory = getRabbitMQGWConnectionFactory();
      connection = factory.newConnection();
      channel = connection.createChannel();

      channel.queuePurge(qname);
      log.info("Purged Queue: " + qname);

    } catch (IOException | TimeoutException e) {
      log.error("Issue deleting message from {} queue.", e, qname);
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

  public void createOutcomeQueues() throws IOException, TimeoutException, InterruptedException {
    Connection connection = null;
    Channel channel = null;

    ConnectionFactory factory = getRabbitMQConnectionFactory();
    connection = factory.newConnection();
    channel = connection.createChannel();

    channel.queueDeclare(FIELD_REFUSALS_QUEUE, false, false, true, null);
    channel.queueDeclare(TEMP_FIELD_OTHERS_QUEUE, false, false, true, null);

    channel.queueBind(FIELD_REFUSALS_QUEUE, GATEWAY_OUTCOME_EXCHANGE, GATEWAY_RESPONDENT_REFUSAL_ROUTING_KEY);

    channel.queueBind(TEMP_FIELD_OTHERS_QUEUE, GATEWAY_OUTCOME_EXCHANGE, GATEWAY_ADDRESS_UPDATE_ROUTING_KEY);
    channel.queueBind(TEMP_FIELD_OTHERS_QUEUE, GATEWAY_OUTCOME_EXCHANGE, GATEWAY_FULFILMENT_REQUEST_ROUTING_KEY);
    channel.queueBind(TEMP_FIELD_OTHERS_QUEUE, GATEWAY_OUTCOME_EXCHANGE, GATEWAY_QUESTIONNAIRE_UPDATE_ROUTING_KEY);
    channel.queueBind(TEMP_FIELD_OTHERS_QUEUE, GATEWAY_OUTCOME_EXCHANGE, GATEWAY_CCS_PROPERTY_LISTING_ROUTING_KEY);
    channel.queueBind(TEMP_FIELD_OTHERS_QUEUE, GATEWAY_OUTCOME_EXCHANGE, GATEWAY_EVENT_FIELDCASE_UPDATE_ROUTING_KEY);
  }

  public NodeCheck doPreFlightCheck() {
    NodeCheckBuilder builder = NodeCheck.builder().name("Rabbit MQ").url(rabbitmqHost);
    try {
      deleteRMMessage(FIELD_REFUSALS_QUEUE);
      builder.isSuccesful(true);
    } catch (Exception e) {
      builder.isSuccesful(false).failureMsg(e.getMessage());
    }    
    return builder.build();
  }
}

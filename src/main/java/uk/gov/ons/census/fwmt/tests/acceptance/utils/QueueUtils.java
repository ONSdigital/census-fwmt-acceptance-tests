package uk.gov.ons.census.fwmt.tests.acceptance.utils;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.rabbitmq.client.AMQP.BasicProperties;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.GetResponse;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class QueueUtils {

  @Value("${service.rabbit.url}")
  private String rabbitmqHost;

  @Value("${service.rabbit.username}")
  private String rabbitmqUsername;

  @Value("${service.rabbit.password}")
  private String rabbitmqPassword;

  @Value("${service.rabbit.port:5672}")
  private int rabbitmqPort;

  @Value("${service.rabbit.virtualHost:/}")
  private String rabbitmqVirtualHost;

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
    return factory;
  }

  public Boolean addMessage(String exchange,
      String routingkey, String message) {
    Connection connection = null;
    Channel channel = null;
    try {
      ConnectionFactory factory = getRabbitMQConnectionFactory();
      connection = factory.newConnection();
      channel = connection.createChannel();

      BasicProperties.Builder builder = new BasicProperties.Builder();
      builder.contentType("text/xml");
      BasicProperties properties = builder.build();

      channel.basicPublish(exchange, routingkey, properties, message.getBytes());
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

  public Boolean deleteMessage(String qname) {
    Connection connection = null;
    Channel channel = null;
    try {
      ConnectionFactory factory = getRabbitMQConnectionFactory();
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
}

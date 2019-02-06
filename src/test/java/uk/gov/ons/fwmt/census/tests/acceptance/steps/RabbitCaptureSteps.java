package uk.gov.ons.fwmt.census.tests.acceptance.steps;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.Consumer;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;
import cucumber.api.java.After;
import cucumber.api.java.en.Given;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.PropertySource;
import uk.gov.ons.fwmt.census.jobservice.data.dto.GatewayEventDTO;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Vector;
import java.util.concurrent.TimeoutException;

@Slf4j
@PropertySource("classpath:application.properties")
public class RabbitCaptureSteps {

  private final static String QUEUE_NAME = "Gateway.Event.Receiver";
  private static final String GATEWAY_EVENTS_EXCHANGE = "Gateway.Events.Exchange";
  private static final String GATEWAY_EVENTS_ROUTING_KEY = "Gateway.Event";
  private final static ObjectMapper OBJECT_MAPPER = new ObjectMapper();
  private List<GatewayEventDTO> eventList = null;
  private Channel channel = null;
  private Connection connection = null;

  @After
  public void tearDown() throws IOException, TimeoutException {
    if (channel != null) {
      channel.close();
      channel = null;
    }
    if (connection != null) {
      connection.close();
      connection = null;
    }
    if (eventList != null) {
      eventList = null;
    }
  }

  @Given("the rabbit monitor is enabled")
  public void theRabbitMonitorIsEnabled() throws IOException, TimeoutException {
    eventList = new Vector<>();
    ConnectionFactory factory = new ConnectionFactory();
    factory.setHost("localhost");
    connection = factory.newConnection();
    channel = connection.createChannel();

    channel.exchangeDeclare(GATEWAY_EVENTS_EXCHANGE, "fanout", true);
    String queueName = channel.queueDeclare().getQueue();
    channel.queueBind(queueName, GATEWAY_EVENTS_EXCHANGE, GATEWAY_EVENTS_ROUTING_KEY);

    Consumer consumer = new DefaultConsumer(channel) {
      @Override
      public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body)
          throws IOException {
        String message = new String(body, StandardCharsets.UTF_8);
        GatewayEventDTO dto = OBJECT_MAPPER.readValue(message.getBytes(), GatewayEventDTO.class);
        eventList.add(dto);
      }
    };
    channel.basicConsume(queueName, true, consumer);
  }
}

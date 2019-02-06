//package uk.gov.ons.fwmt.census.config;
//
//import com.fasterxml.jackson.databind.ObjectMapper;
//import com.rabbitmq.client.Channel;
//import com.rabbitmq.client.ConnectionFactory;
//import com.rabbitmq.client.Connection;
//import com.rabbitmq.client.DeliverCallback;
//import uk.gov.ons.fwmt.census.jobservice.data.dto.GatewayEventDTO;
//
//public class RabbitConfig {
//
//  private final static String QUEUE_NAME = "Gateway.Event.Receiver";
//
//  private static final String GATEWAY_EVENTS_EXCHANGE = "Gateway.Events.Exchange";
//
//  private static final String GATEWAY_EVENTS_ROUTING_KEY = "Gateway.Event";
//
//  private final static  ObjectMapper OBJECT_MAPPER = new ObjectMapper();
//
//  public static void main(String args[]) throws Exception{
//
//    ConnectionFactory factory = new ConnectionFactory();
//    factory.setHost("localhost");
//    Connection connection = factory.newConnection();
//    Channel channel = connection.createChannel();
//
//    channel.exchangeDeclare(GATEWAY_EVENTS_EXCHANGE, "fanout",true);
//    String queueName = channel.queueDeclare().getQueue();
//    channel.queueBind(queueName, GATEWAY_EVENTS_EXCHANGE, GATEWAY_EVENTS_ROUTING_KEY);
//
//    System.out.println(" [*] Waiting for messages. To exit press CTRL+C");
//
//    DeliverCallback deliverCallback = (consumerTag, delivery) -> {
//
//      String message = new String(delivery.getBody(), "UTF-8");
//      System.out.println(message);
//
//      GatewayEventDTO dto = OBJECT_MAPPER.readValue(message.getBytes(), GatewayEventDTO.class);
//      System.out.println(" Received '" + dto.getCaseId()  + ":"  + dto.getEventType() + ":") ;
//
//    };
//    channel.basicConsume(queueName, true, deliverCallback, consumerTag -> { });
//  }
//}
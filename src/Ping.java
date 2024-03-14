import com.rabbitmq.client.*;

public class Ping implements Message_itf{

    private static final String PING_QUEUE_NAME = "ping_queue";
    private static final String PONG_QUEUE_NAME = "pong_queue";
    private static final String PING_MESSAGE = "PING";

    public static void main(String[] argv) throws Exception {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");
        Connection connection = factory.newConnection();
        Channel channel = connection.createChannel();

        channel.queueDeclare(PING_QUEUE_NAME, false, false, false, null);
        channel.queueDeclare(PONG_QUEUE_NAME, false, false, false, null);
        Message_itf.sendMessage(channel, PING_QUEUE_NAME, PING_MESSAGE);

        DeliverCallback pongCallback = (consumerTag, delivery) -> {
            String message = new String(delivery.getBody(), "UTF-8");
            System.out.println("Received: [" + message + "]");
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            Message_itf.sendMessage(channel, PING_QUEUE_NAME, PING_MESSAGE);
        };
        channel.basicConsume(PONG_QUEUE_NAME, true, pongCallback, consumerTag -> {
        });
    }
    
}

import com.rabbitmq.client.*;

public class Pong implements Message_itf {

    private static final String PING_QUEUE_NAME = "ping_queue";
    private static final String PONG_QUEUE_NAME = "pong_queue";
    private static final String PONG_MESSAGE = "PONG";

    public static void main(String[] argv) throws Exception {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");
        Connection connection = factory.newConnection();
        Channel channel = connection.createChannel();

        channel.queueDeclare(PING_QUEUE_NAME, false, false, false, null);
        channel.queueDeclare(PONG_QUEUE_NAME, false, false, false, null);

        DeliverCallback pingCallback = (consumerTag, delivery) -> {
            String message = new String(delivery.getBody(), "UTF-8");
            System.out.println("Received: [" + message + "]");
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            Message_itf.sendMessage(channel, PONG_QUEUE_NAME, PONG_MESSAGE);
        };
        channel.basicConsume(PING_QUEUE_NAME, true, pingCallback, consumerTag -> {
        });
    }
}

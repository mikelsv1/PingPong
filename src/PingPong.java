import com.rabbitmq.client.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class PingPong {
    private static final String INIT_QUEUE_NAME_1 = "init_queue_1";
    private static final String INIT_QUEUE_NAME_2 = "init_queue_2";
    private static final String PING_QUEUE_NAME = "ping_queue";
    private static final String PONG_QUEUE_NAME = "pong_queue";

    private static final String INIT_MESSAGE = "init_conn";
    private static final String OK_MESSAGE = "ok_conn";
    private static final String PING_MESSAGE = "ping";
    private static final String PONG_MESSAGE = "pong";

    private static boolean isStarted = false;
    private static String nodeId;

    private static List<Integer> nodeIds = new ArrayList<Integer>();

    public static void main(String[] argv) throws Exception {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");
        Connection connection = factory.newConnection();
        Channel channel = connection.createChannel();

        channel.queueDeclare(INIT_QUEUE_NAME_1, false, false, false, null);
        channel.queueDeclare(INIT_QUEUE_NAME_2, false, false, false, null);
        channel.queueDeclare(PING_QUEUE_NAME, false, false, false, null);
        channel.queueDeclare(PONG_QUEUE_NAME, false, false, false, null);

        nodeId = Integer.toString((int) (Math.random() * 10000));
        nodeIds.add(Integer.parseInt(nodeId));

        sendMessage(channel, INIT_QUEUE_NAME_1, INIT_MESSAGE + ":" + nodeId);
        sendMessage(channel, INIT_QUEUE_NAME_2, INIT_MESSAGE + ":" + nodeId);

        DeliverCallback initCallback = (consumerTag, delivery) -> {
            String message = new String(delivery.getBody(), "UTF-8");
            System.out.println("Received: '" + message + "'");
            String[] parts = message.split(":");
            String receivedId = parts[1];
            if (!isStarted) {
                if(!nodeIds.contains(Integer.parseInt(receivedId))) {
                    nodeIds.add(Integer.parseInt(receivedId));
                }
                sendMessage(channel, INIT_QUEUE_NAME_1, INIT_MESSAGE + ":" + nodeId);
                sendMessage(channel, INIT_QUEUE_NAME_2, INIT_MESSAGE + ":" + nodeId);
                if (receivedId.compareTo(nodeId) < 0) {
                    sendMessage(channel, INIT_QUEUE_NAME_1, INIT_MESSAGE + ":" + nodeId);
                    sendMessage(channel, INIT_QUEUE_NAME_2, INIT_MESSAGE + ":" + nodeId);
                } else if (receivedId.compareTo(nodeId) > 0) {
                    sendMessage(channel, INIT_QUEUE_NAME_1, OK_MESSAGE);
                    sendMessage(channel, INIT_QUEUE_NAME_2, OK_MESSAGE);
                    isStarted = true;
                    sendMessage(channel, PING_QUEUE_NAME, PING_MESSAGE);
                }
            }
        };
        channel.basicConsume(INIT_QUEUE_NAME_1, true, initCallback, consumerTag -> {
        });
        channel.basicConsume(INIT_QUEUE_NAME_2, true, initCallback, consumerTag -> {
        });
        
        DeliverCallback pingCallback = (consumerTag, delivery) -> {
            String message = new String(delivery.getBody(), "UTF-8");
            System.out.println("Received: '" + message + "'");
            sendMessage(channel, PONG_QUEUE_NAME, PONG_MESSAGE);
        };
        channel.basicConsume(PING_QUEUE_NAME, true, pingCallback, consumerTag -> {
        });

        DeliverCallback pongCallback = (consumerTag, delivery) -> {
            String message = new String(delivery.getBody(), "UTF-8");
            System.out.println("Received: '" + message + "'");
            sendMessage(channel, PING_QUEUE_NAME, PING_MESSAGE);
        };
        channel.basicConsume(PONG_QUEUE_NAME, true, pongCallback, consumerTag -> {
        });
    }

    private static void sendMessage(Channel channel, String queueName, String message) throws IOException {
        channel.basicPublish("", queueName, null, message.getBytes("UTF-8"));
        System.out.println("Sent: '" + message + "'");
    }
}

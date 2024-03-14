import java.io.IOException;

import com.rabbitmq.client.Channel;

public interface Message_itf {
    public static void sendMessage(Channel channel, String queueName, String message) throws IOException {
        channel.basicPublish("", queueName, null, message.getBytes());
        System.out.println("Sent: [" + message + "]");
    }
}

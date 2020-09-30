package topic;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.itextpdf.text.DocumentException;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import model.User;
import org.apache.commons.lang3.RandomStringUtils;
import utils.Pdf;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.util.UUID;
import java.util.concurrent.TimeoutException;

public class ConsumerChildrenTopic {
    private final static String DOCUMENTS_ROUTING_KEY = "account.children.*";
    private final static String FILES_EXCHANGE = "files_topic_exchange";

    private final static String packageName = "pdf_package№" + RandomStringUtils.random(5, false, true);


    public static void main(String[] args) {
        ConnectionFactory connectionFactory = new ConnectionFactory();
        connectionFactory.setHost("localhost");
        Pdf pdf = new Pdf();
        try {
            Connection connection = connectionFactory.newConnection();
            Channel channel = connection.createChannel();
            channel.basicQos(3);

            // объявляем очередь
            String queueName = channel.queueDeclare().getQueue();
            // привязываем очередь с Exchange по ключу files.documents.*
            channel.queueBind(queueName, FILES_EXCHANGE, DOCUMENTS_ROUTING_KEY);
            // когда получили сообщения
            channel.basicConsume(queueName, false, (consumerTag, message) -> {
                String jsonUser = new String(message.getBody());
                //json в юзера , заполнение pdf
                ObjectMapper mapper = new ObjectMapper();
                try {
                    User user = mapper.readValue(jsonUser, User.class);
                    pdf.addPdf(user, "DoctorChildren.txt", packageName);
                    channel.basicAck(message.getEnvelope().getDeliveryTag(), false);
                } catch (IOException | DocumentException e) {
                    System.err.println("FAILED");
                    channel.basicReject(message.getEnvelope().getDeliveryTag(), false);
                }

            }, consumerTag -> {});
        } catch (IOException | TimeoutException e) {
            throw new IllegalArgumentException(e);
        }
    }
}

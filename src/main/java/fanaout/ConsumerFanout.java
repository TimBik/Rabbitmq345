package fanaout;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.itextpdf.text.*;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DeliverCallback;
import model.User;
import org.apache.commons.lang3.RandomStringUtils;
import utils.Pdf;

import java.io.*;
import java.util.concurrent.TimeoutException;

public class ConsumerFanout {
    private final static String EXCHANGE_NAME = "pdf";
    private final static String EXCHANGE_TYPE = "fanout";
    private final static String packageName = "pdf_package№" + RandomStringUtils.random(5, false, true);

    public static void main(String[] args) {
        ConnectionFactory connectionFactory = new ConnectionFactory();
        connectionFactory.setHost("localhost");
        try {
            Connection connection = connectionFactory.newConnection();
            Channel channel = connection.createChannel();
            channel.basicQos(3);

            channel.exchangeDeclare(EXCHANGE_NAME, EXCHANGE_TYPE);
            // создаем временную очередь со случайным названием
            String queue = channel.queueDeclare().getQueue();

            // привязали очередь к EXCHANGE_NAME
            channel.queueBind(queue, EXCHANGE_NAME, "");

            DeliverCallback deliverCallback = (consumerTag, message) -> {
                String jsonUser = new String(message.getBody());
                //json в юзера , заполнение pdf
                ObjectMapper mapper = new ObjectMapper();
                Pdf pdf = new Pdf();
                try {
                    User user = mapper.readValue(jsonUser, User.class);
                    pdf.addPdf(user, "Hello.txt", packageName);
                    channel.basicAck(message.getEnvelope().getDeliveryTag(), false);
                } catch (IOException | DocumentException e) {
                    System.out.println(e.getMessage());
                    System.err.println("FAILED");
                    channel.basicReject(message.getEnvelope().getDeliveryTag(), false);
                }
            };

            channel.basicConsume(queue, false, deliverCallback, consumerTag -> {});
        } catch (IOException | TimeoutException e) {
            throw new IllegalArgumentException(e);
        }
    }
}

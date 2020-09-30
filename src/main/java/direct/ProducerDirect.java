package direct;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import model.User;
import java.io.IOException;
import java.util.Scanner;
import java.util.concurrent.TimeoutException;

public class ProducerDirect {
    // есть очередь для справки
    private final static String REFERENCE_QUEUE = "reference_queue";
    // есть отдельная очередь для освобождения от физ-ры
    private final static String EXEMPTION_QUEUE = "exemption_queue";

    // роутинг по png
    private final static String REFERENCE_ROUTING_KEY = "reference";
    // роутинг по jpg
    private final static String EXEMPTION_ROUTING_KEY = "exemption";
    // есть новый экчендж
    private final static String DOCUMENT_EXCHANGE = "document_direct_exchange";
    // новый
    private final static String EXCHANGE_TYPE = "direct";


    public static void main(String[] args) {
        ConnectionFactory connectionFactory = new ConnectionFactory();
        connectionFactory.setHost("localhost");
        Scanner sc = new Scanner(System.in);
        while (true) {
            System.out.println("Выберите Cправка или Освобождение от физ-ры (input:1 or 2)");
            String type = sc.nextLine();
            System.out.println("Введите имя");
            String name = sc.nextLine();
            System.out.println("Введите фамилию");
            String surname = sc.nextLine();
            System.out.println("Введите болезнь");
            String illness = sc.nextLine();
            System.out.println("Введите дату начала болезни");
            String first = sc.nextLine();
            System.out.println("Введите дату конца болезни");
            String last = sc.nextLine();
            User user = new User(name,surname,illness,first,last);
            //создание юзера с болезнью и числами
            try {
                Connection connection = connectionFactory.newConnection();
                Channel channel = connection.createChannel();
                // создали Exchange
                channel.exchangeDeclare(DOCUMENT_EXCHANGE, EXCHANGE_TYPE);
                // привязали очереди под определенным routingKey
                channel.queueBind(REFERENCE_QUEUE, DOCUMENT_EXCHANGE, REFERENCE_ROUTING_KEY);
                channel.queueBind(EXEMPTION_QUEUE, DOCUMENT_EXCHANGE, EXEMPTION_ROUTING_KEY);
                ObjectMapper mapper = new ObjectMapper();
                String jsonObject = mapper.writeValueAsString(user);
                String currentRouting = "";
                if (type.equals("1")) {
                    currentRouting = REFERENCE_ROUTING_KEY;
                } else if (type.equals("2")) {
                    currentRouting = EXEMPTION_ROUTING_KEY;
                } else System.err.println("Попробуйте ещё раз"); // отправка юзера ту ду
                channel.basicPublish(DOCUMENT_EXCHANGE, currentRouting, null, jsonObject.getBytes());
            } catch (IOException | TimeoutException e) {
                throw new IllegalArgumentException(e);
            }
        }
    }
}

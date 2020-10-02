package topic;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import model.User;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Scanner;
import java.util.concurrent.TimeoutException;

public class ProducerTopic {
    private final static String CHILDREN_ROUTING_KEY = "account.children.";
    private final static String ADULTS_ROUTING_KEY = "account.adults.";

    private final static String FILES_EXCHANGE = "account_topic_exchange";
    private final static String EXCHANGE_TYPE = "topic";

    public static void main(String[] args) {
        ConnectionFactory connectionFactory = new ConnectionFactory();
        connectionFactory.setHost("localhost");
        Scanner sc = new Scanner(System.in);
        while (true) {
            System.out.println("Введите имя");
            String name = sc.nextLine();
            System.out.println("Введите фамилию");
            String surname = sc.nextLine();
            System.out.println("Введите номер паспорта");
            String number = sc.nextLine();
            System.out.println("Введите возраст");
            String age = sc.nextLine();
            System.out.println("Введите медицинский полис");
            String polis = sc.nextLine();
            System.out.println("Введите врача к которому хотите записаться (Пример: Терапевт)");
            String doctor = sc.nextLine();
            System.out.println("Укажите время и дату когда вы хотите подойти к данному врачу (Пример: 19:15 21.08.2020)");
            String time = sc.nextLine();
            User user = new User(name,surname,number,Integer.parseInt(age) ,polis,doctor,time);
            try {
                Connection connection = connectionFactory.newConnection();
                Channel channel = connection.createChannel();
                ObjectMapper mapper = new ObjectMapper();
                String jsonObject = mapper.writeValueAsString(user);
                channel.exchangeDeclare(FILES_EXCHANGE, EXCHANGE_TYPE);

                // формируем роутинг
                String currentRouting = "";
                if (user.getAge() < 18) {
                    currentRouting = CHILDREN_ROUTING_KEY + doctor;
                } else if (user.getAge() >= 18) {
                    currentRouting = ADULTS_ROUTING_KEY + doctor;
                } else System.err.println("Попробуйте ещё раз");
                //формируем юзера, конвертируем в джсон и отправляем ту ду
                // отправляет сообщение, которое содержит URL-файла с нужным роутингом
                channel.basicPublish(FILES_EXCHANGE, currentRouting, null, jsonObject.getBytes());

            } catch (IOException | TimeoutException e) {
                throw new IllegalArgumentException(e);
            }
        }
    }
}

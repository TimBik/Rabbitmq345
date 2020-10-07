import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import model.User;

import java.io.IOException;
import java.util.Scanner;
import java.util.concurrent.TimeoutException;

public class Producer {
    // есть EXCHANGE - images НЕ ОЧЕРЕДЬ
    private final static String EXCHANGE_NAME = "pdf";
    // тип FANOUT
    private final static String EXCHANGE_TYPE_FANOUT = "fanout";
    // роутинг по png
    private final static String REFERENCE_ROUTING_KEY = "reference";
    // роутинг по jpg
    private final static String EXEMPTION_ROUTING_KEY = "exemption";
    // есть новый экчендж
    private final static String DOCUMENT_EXCHANGE = "document_direct_exchange";
    // новый
    private final static String EXCHANGE_TYPE_DIRECT = "direct";
    private final static String CHILDREN_ROUTING_KEY = "account.children.";
    private final static String ADULTS_ROUTING_KEY = "account.adults.";

    private final static String FILES_EXCHANGE = "account_topic_exchange";
    private final static String EXCHANGE_TYPE_TOPIC = "topic";

    public static void main(String[] args) {
        ConnectionFactory connectionFactory = new ConnectionFactory();
        connectionFactory.setHost("localhost");
        Scanner sc = new Scanner(System.in);
        ObjectMapper mapper = new ObjectMapper();
        String currentRouting = "";
        while (true) {
            System.out.println("Введите имя");
            String name = sc.nextLine();
            System.out.println("Введите фамилию");
            String surname = sc.nextLine();
            System.out.println("Введите номер паспорта");
            String number = sc.nextLine();
            System.out.println("Введите возраст");
            String age = sc.nextLine();
            //проверку сделать на все
            System.out.println("Введите медицинский полис");
            String polis = sc.nextLine();
            System.out.println("Выберите цель визита: Справка или Записаться ко врачу (input 1 or 2)");
            String purpose = sc.nextLine();
            User userF = new User(name, surname, number, Integer.parseInt(age), polis);
            User userD = new User();
            User userT = new User();
            String type = "";
            if (purpose.equals("1")) {
                System.out.println("Выберите Cправка или Освобождение от физ-ры (input 1 or 2)");
                type = sc.nextLine();
                System.out.println("Введите болезнь");
                String illness = sc.nextLine();
                System.out.println("Введите дату начала болезни");
                String first = sc.nextLine();
                System.out.println("Введите дату конца болезни");
                String last = sc.nextLine();
                userD = new User(name,surname,illness,first,last);
            } else if (purpose.equals("2")) {
                System.out.println("Введите врача к которому хотите записаться (Пример: Терапевт)");
                String doctor = sc.nextLine();
                System.out.println("Укажите время и дату когда вы хотите подойти к данному врачу (Пример: 19:15 21.08.2020)");
                String time = sc.nextLine();
                userT = new User(name,surname,number,Integer.parseInt(age),polis,doctor,time);
                // формируем роутинг
                if (userT.getAge() < 18) {
                    currentRouting = CHILDREN_ROUTING_KEY + doctor;
                } else if (userT.getAge() >= 18) {
                    currentRouting = ADULTS_ROUTING_KEY + doctor;
                } else System.err.println("Попробуйте ещё раз");
                //формируем юзера, конвертируем в джсон и отправляем ту ду
                // отправляет сообщение, которое содержит URL-файла с нужным роутингом
            } else System.err.println("Попробуйте ещё раз"); //на начало while
            String jsonObject = "";
            try {
                jsonObject = mapper.writeValueAsString(userF);
                fanout(connectionFactory,jsonObject);
                if (purpose.equals("1")) {
                    jsonObject = mapper.writeValueAsString(userD);
                    direct(connectionFactory,jsonObject,type);
                } else if (purpose.equals("2")) {
                    jsonObject = mapper.writeValueAsString(userT);
                    topic(connectionFactory, jsonObject, currentRouting);
                }
            } catch (IOException e) {
                throw new IllegalArgumentException(e);
            }
        }
    }
    public static void fanout(ConnectionFactory connectionFactory, String jsonObject) {
        try {
            Connection connection = connectionFactory.newConnection();
            Channel channel = connection.createChannel();
            // создаем exchange
            channel.exchangeDeclare(EXCHANGE_NAME, EXCHANGE_TYPE_FANOUT);
            channel.basicPublish(EXCHANGE_NAME, "", null, jsonObject.getBytes());
        } catch (IOException | TimeoutException e) {
            throw new IllegalArgumentException(e);
        }
    }
    public static void direct(ConnectionFactory connectionFactory,String jsonObject, String type) {
        try {
            Connection connection = connectionFactory.newConnection();
            Channel channel = connection.createChannel();
            // создали Exchange
            channel.exchangeDeclare(DOCUMENT_EXCHANGE, EXCHANGE_TYPE_DIRECT);
            String currentRouting = "";
            if (type.equals("1")) {
                currentRouting = REFERENCE_ROUTING_KEY;
            } else if (type.equals("2")) {
                currentRouting = EXEMPTION_ROUTING_KEY;
            } else System.err.println("Попробуйте ещё раз"); // отправка юзера ту ду
            channel.basicPublish(DOCUMENT_EXCHANGE, currentRouting, null, jsonObject.getBytes());
        }catch (IOException | TimeoutException e) {
            throw new IllegalArgumentException(e);
        }
    }
    public static void topic(ConnectionFactory connectionFactory,String jsonObject, String currentRouting) {
        try {
            Connection connection = connectionFactory.newConnection();
            Channel channel = connection.createChannel();
            channel.exchangeDeclare(FILES_EXCHANGE, EXCHANGE_TYPE_TOPIC);
            channel.basicPublish(FILES_EXCHANGE, currentRouting, null, jsonObject.getBytes());
        } catch (IOException | TimeoutException e) {
            throw new IllegalArgumentException(e);
        }
    }
}

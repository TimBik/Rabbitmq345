package fanaout;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import direct.ProducerDirect;
import model.User;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Scanner;
import java.util.concurrent.TimeoutException;

public class ProducerFanout {
    // есть EXCHANGE - images НЕ ОЧЕРЕДЬ
    private final static String EXCHANGE_NAME = "pdf";
    // тип FANOUT
    private final static String EXCHANGE_TYPE = "fanout";

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
            /*System.out.println("Выберите цель визита (Справка или Записаться ко врачу");
            String purpose = sc.nextLine();*/
            User user = new User(name, surname, number, Integer.parseInt(age), polis);
            try {
                Connection connection = connectionFactory.newConnection();
                Channel channel = connection.createChannel();
                // создаем exchange
                channel.exchangeDeclare(EXCHANGE_NAME, EXCHANGE_TYPE);
                ObjectMapper mapper = new ObjectMapper();
                String jsonObject = mapper.writeValueAsString(user);
                //считывание с клавиатуры создание юзера юзера превращаем в json
                channel.basicPublish(EXCHANGE_NAME, "", null, jsonObject.getBytes());
            } catch (IOException | TimeoutException e) {
                throw new IllegalArgumentException(e);
            }
            /*if (purpose.equals("Справка")) {

            } else {

            }*/
        }
    }
}

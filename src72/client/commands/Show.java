package client.commands;

import client.managers.NetworkManager;
import common.CommandRequest;
import common.CommandResponse;
import common.CommandType;
import common.City;

import java.net.DatagramSocket;

public class Show {
    public CommandResponse execute(DatagramSocket socket,
                                   NetworkManager networkManager,
                                   String host,
                                   int port,
                                   String login,
                                   String password) {
        try {
            CommandRequest request = new CommandRequest(
                    CommandType.SHOW,
                    new String[0],
                    null,
                    login,
                    password
            );

            System.out.println("Запрос коллекции...");
            CommandResponse response = networkManager.sendRequest(
                    socket, request, host, port);

            return response != null ? response :
                    new CommandResponse(false, "Сервер не ответил");

        } catch (Exception e) {
            return new CommandResponse(false,
                    "Ошибка отправки запроса: " + e.getMessage());
        }
    }

    public void printResponse(CommandResponse response) {
        System.out.println(response.getMessage());

        if (response.hasData() && response.getData() != null) {
            System.out.println("=== Все элементы коллекции ===");
            System.out.println("ID | Название | Координаты | Дата создания | Площадь | Население | Высота | Климат | Правительство | Уровень жизни | Губернатор");

            for (City city : response.getData()) {
                System.out.println(city.toString());
            }

            System.out.println("================================");
            System.out.println("Всего элементов: " + response.getData().size());
        }
    }
}
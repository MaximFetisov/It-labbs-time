package client.commands;

import client.managers.NetworkManager;
import common.CommandRequest;
import common.CommandResponse;
import common.CommandType;

import java.net.DatagramSocket;

/**
 * Клиентская команда RemoveFirst.
 * Отправляет запрос на сервер для удаления первого элемента коллекции.
 */
public class RemoveFirst {

    public CommandResponse execute(DatagramSocket socket,
                                   NetworkManager networkManager,
                                   String host,
                                   int port,
                                   String login,
                                   String password) {
        try {
            CommandRequest request = new CommandRequest(
                    CommandType.REMOVE_FIRST,
                    new String[0],
                    null,
                    login,
                    password
            );

            System.out.println("Запрос на удаление первого элемента...");
            CommandResponse response = networkManager.sendRequest(
                    socket, request, host, port);

            return response != null ? response :
                    new CommandResponse(false, "Сервер не ответил");

        } catch (Exception e) {
            return new CommandResponse(false,
                    "Ошибка отправки запроса: " + e.getMessage());
        }
    }
}
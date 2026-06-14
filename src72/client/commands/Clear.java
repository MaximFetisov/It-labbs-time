package client.commands;

import client.managers.NetworkManager;
import common.CommandRequest;
import common.CommandResponse;
import common.CommandType;

import java.net.DatagramSocket;

/**
 * Клиентская команда Clear.
 * Отправляет запрос на сервер для очистки коллекции, принадлежащей текущему пользователю.
 * Передаёт данные авторизации для проверки прав доступа.
 */
public class Clear {

    public CommandResponse execute(DatagramSocket socket,
                                   NetworkManager networkManager,
                                   String host,
                                   int port,
                                   String login,
                                   String password) {
        try {
            // Формируем запрос. Город не передаётся, передаются только учётные данные
            CommandRequest request = new CommandRequest(
                    CommandType.CLEAR,
                    new String[0],
                    null,
                    login,
                    password
            );

            System.out.println("Отправка запроса на очистку коллекции...");
            CommandResponse response = networkManager.sendRequest(socket, request, host, port);

            return response != null ? response : new CommandResponse(false, "Сервер не ответил");

        } catch (Exception e) {
            return new CommandResponse(false, "Ошибка отправки запроса: " + e.getMessage());
        }
    }
}
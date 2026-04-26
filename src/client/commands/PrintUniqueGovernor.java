package client.commands;

import client.managers.NetworkManager;
import common.CommandRequest;
import common.CommandResponse;
import common.CommandType;

import java.net.DatagramSocket;

/**
 * Клиентская команда PrintUniqueGovernor.
 * Отправляет запрос на сервер для получения уникальных губернаторов.
 *
 * @author Максим
 * @see NetworkManager
 */
public class PrintUniqueGovernor {

    /**
     * Выполняет команду получения уникальных губернаторов.
     *
     * @param socket сокет для отправки запроса
     * @param networkManager менеджер сети
     * @param host хост сервера
     * @param port порт сервера
     * @return результат выполнения команды
     */
    public CommandResponse execute(DatagramSocket socket,
                                   NetworkManager networkManager,
                                   String host,
                                   int port) {
        try {
            // Создание запроса без аргументов и без объекта
            CommandRequest request = new CommandRequest(
                    CommandType.PRINT_UNIQUE_GOVERNOR,
                    new String[0],
                    null
            );

            // Отправка на сервер и получение ответа
            System.out.println("Запрос уникальных губернаторов...");
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
package client.commands;

import client.managers.NetworkManager;
import common.CommandRequest;
import common.CommandResponse;
import common.CommandType;

import java.net.DatagramSocket;

/**
 * Клиентская команда SumOfMeters.
 * Отправляет запрос на сервер для получения суммы метров над уровнем моря.
 *
 * @author Максим
 * @see NetworkManager
 */
public class SumOfMeters {

    /**
     * Выполняет команду подсчёта суммы метров над уровнем моря.
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
            CommandRequest request = new CommandRequest(
                    CommandType.SUM_OF_METERS_ABOVE_SEA_LEVEL,
                    new String[0],
                    null
            );

            System.out.println("Запрос суммы метров над уровнем моря...");
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
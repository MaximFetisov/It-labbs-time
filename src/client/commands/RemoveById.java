package client.commands;

import client.managers.NetworkManager;
import common.CommandRequest;
import common.CommandResponse;
import common.CommandType;

import java.net.DatagramSocket;

/**
 * Клиентская команда RemoveById.
 * Отправляет запрос на сервер для удаления элемента по ID.
 *
 * @author Максим
 * @see NetworkManager
 */
public class RemoveById {

    /**
     * Выполняет команду удаления элемента по ID.
     *
     * @param socket сокет для отправки запроса
     * @param networkManager менеджер сети
     * @param host хост сервера
     * @param port порт сервера
     * @param id идентификатор элемента для удаления
     * @return результат выполнения команды
     */
    public CommandResponse execute(DatagramSocket socket,
                                   NetworkManager networkManager,
                                   String host,
                                   int port,
                                   long id) {
        try {
            CommandRequest request = new CommandRequest(
                    CommandType.REMOVE_BY_ID,
                    new String[]{String.valueOf(id)},
                    null
            );

            System.out.println("Отправка запроса на удаление элемента с ID " + id + "...");
            CommandResponse response = networkManager.sendRequest(
                    socket, request, host, port);

            return response != null ? response :
                    new CommandResponse(false, "Сервер не ответил");

        } catch (Exception e) {
            return new CommandResponse(false,
                    "Ошибка отправки запроса: " + e.getMessage());
        }
    }

    /**
     * Выполняет команду удаления элемента по ID (без явной передачи ID).
     * ID берётся из аргументов командной строки клиента.
     *
     * @param socket сокет для отправки запроса
     * @param networkManager менеджер сети
     * @param host хост сервера
     * @param port порт сервера
     * @param args аргументы команды (должны содержать ID)
     * @return результат выполнения команды
     */
    public CommandResponse executeWithArgs(DatagramSocket socket,
                                           NetworkManager networkManager,
                                           String host,
                                           int port,
                                           String[] args) {
        try {
            if (args == null || args.length == 0) {
                return new CommandResponse(false,
                        "Ошибка: не указан ID элемента для удаления");
            }


            try {
                Long.parseLong(args[0]);
            } catch (NumberFormatException e) {
                return new CommandResponse(false,
                        "Ошибка: ID должен быть числом типа long");
            }

            CommandRequest request = new CommandRequest(
                    CommandType.REMOVE_BY_ID,
                    args,
                    null
            );

            System.out.println("Отправка запроса на удаление элемента с ID " + args[0] + "...");
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
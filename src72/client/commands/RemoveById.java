package client.commands;

import client.managers.NetworkManager;
import common.CommandRequest;
import common.CommandResponse;
import common.CommandType;

import java.net.DatagramSocket;

/**
 * Клиентская команда RemoveById.
 * Отправляет запрос на сервер для удаления элемента по ID.
 * Передаёт данные авторизации (login/password) для проверки прав доступа.
 */
public class RemoveById {

    /**
     * Выполняет команду удаления элемента по ID.
     *
     * @param socket         сокет для отправки запроса
     * @param networkManager менеджер сети
     * @param host           хост сервера
     * @param port           порт сервера
     * @param id             идентификатор элемента для удаления
     * @param login          логин пользователя
     * @param password       пароль пользователя
     * @return результат выполнения команды
     */
    public CommandResponse execute(DatagramSocket socket,
                                   NetworkManager networkManager,
                                   String host,
                                   int port,
                                   long id,
                                   String login,
                                   String password) {
        try {
            // Формируем запрос. Обратите внимание на передачу login и password
            CommandRequest request = new CommandRequest(
                    CommandType.REMOVE_BY_ID,
                    new String[]{String.valueOf(id)},
                    null, // Объект City не нужен
                    login,
                    password
            );

            System.out.println("Отправка запроса на удаление элемента с ID " + id + "...");
            CommandResponse response = networkManager.sendRequest(socket, request, host, port);

            return response != null ? response : new CommandResponse(false, "Сервер не ответил.");

        } catch (Exception e) {
            return new CommandResponse(false, "Ошибка отправки запроса: " + e.getMessage());
        }
    }

    /**
     * Выполняет команду удаления элемента по ID (с аргументами из командной строки или скрипта).
     */
    public CommandResponse executeWithArgs(DatagramSocket socket,
                                           NetworkManager networkManager,
                                           String host,
                                           int port,
                                           String[] args,
                                           String login,
                                           String password) {
        try {
            if (args == null || args.length == 0) {
                return new CommandResponse(false, "Ошибка: не указан ID элемента для удаления.");
            }

            long id;
            try {
                id = Long.parseLong(args[0]);
            } catch (NumberFormatException e) {
                return new CommandResponse(false, "Ошибка: ID должен быть числом типа long.");
            }

            // Вызываем основной метод execute
            return execute(socket, networkManager, host, port, id, login, password);

        } catch (Exception e) {
            return new CommandResponse(false, "Ошибка выполнения команды: " + e.getMessage());
        }
    }
}
package client.commands;

import client.managers.NetworkManager;
import common.CommandRequest;
import common.CommandResponse;
import common.CommandType;

import java.net.DatagramSocket;

public class PrintUniqueGovernor {
    public CommandResponse execute(DatagramSocket socket,
                                   NetworkManager networkManager,
                                   String host,
                                   int port,
                                   String login,
                                   String password) {
        try {
            CommandRequest request = new CommandRequest(
                    CommandType.PRINT_UNIQUE_GOVERNOR,
                    new String[0],
                    null,
                    login,
                    password
            );

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
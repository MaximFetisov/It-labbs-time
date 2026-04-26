package client.commands;

import client.forms.CityForm;
import client.managers.NetworkManager;
import common.CommandRequest;
import common.CommandResponse;
import common.CommandType;
import common.City;

import java.net.DatagramSocket;

public class Update {

    public CommandResponse execute(DatagramSocket socket,
                                   NetworkManager networkManager,
                                   String host,
                                   int port,
                                   long id) {
        try {
            System.out.println("Проверка существования элемента с ID " + id + "...");

            CommandRequest showRequest = new CommandRequest(
                    CommandType.SHOW,
                    new String[0],
                    null
            );

            CommandResponse showResponse = networkManager.sendRequest(
                    socket, showRequest, host, port);

            if (showResponse == null) {
                return new CommandResponse(false, "Сервер не ответил");
            }

            // Проверяем, существует ли элемент с таким ID
            boolean exists = false;
            if (showResponse.hasData() && showResponse.getData() != null) {
                for (City city : showResponse.getData()) {
                    if (city.getId() == id) {
                        exists = true;
                        break;
                    }
                }
            }

            // Если элемент не найден — сразу возвращаем ошибку
            if (!exists) {
                return new CommandResponse(false,
                        "Элемент с ID " + id + " не найден в коллекции. " +
                                "Проверьте ID командой 'show' и попробуйте снова");
            }

            // Элемент существует, теперь запрашиваем данные
            System.out.println("Элемент найден. Введите данные для обновления:");
            CityForm cityForm = new CityForm();
            City city = cityForm.buildForUpdate();

            if (city == null) {
                return new CommandResponse(false,
                        "Ошибка при создании объекта City");
            }

            CommandRequest request = new CommandRequest(
                    CommandType.UPDATE,
                    new String[]{String.valueOf(id)},
                    city
            );

            System.out.println("Отправка запроса на обновление элемента...");
            CommandResponse response = networkManager.sendRequest(
                    socket, request, host, port);

            return response != null ? response :
                    new CommandResponse(false, "Сервер не ответил");

        } catch (Exception e) {
            return new CommandResponse(false,
                    "Ошибка отправки запроса: " + e.getMessage());
        }
    }

    public CommandResponse executeWithArgs(DatagramSocket socket,
                                           NetworkManager networkManager,
                                           String host,
                                           int port,
                                           String[] args) {
        try {
            if (args == null || args.length == 0) {
                return new CommandResponse(false,
                        "Ошибка: не указан ID элемента для обновления");
            }

            long id;
            try {
                id = Long.parseLong(args[0]);
            } catch (NumberFormatException e) {
                return new CommandResponse(false,
                        "Ошибка: ID должен быть числом типа long");
            }

            return execute(socket, networkManager, host, port, id);

        } catch (Exception e) {
            return new CommandResponse(false,
                    "Ошибка отправки запроса: " + e.getMessage());
        }
    }
}
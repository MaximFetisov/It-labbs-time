package client.commands;

import client.forms.CityForm;
import client.managers.NetworkManager;
import common.CommandRequest;
import common.CommandResponse;
import common.CommandType;
import common.City;

import java.net.DatagramSocket;

/**
 * Клиентская команда Update.
 * Отправляет запрос на сервер для обновления элемента по ID.
 * Передаёт данные авторизации для проверки прав доступа.
 */
public class Update {

    public CommandResponse execute(DatagramSocket socket,
                                   NetworkManager networkManager,
                                   String host,
                                   int port,
                                   long id,
                                   String login,
                                   String password) {
        try {
            System.out.println("Введите новые данные для обновления элемента с ID " + id + ":");

            CityForm cityForm = new CityForm();
            City city = cityForm.buildForUpdate();

            if (city == null) {
                return new CommandResponse(false, "Ошибка при создании объекта City");
            }

            // Формируем запрос с ID, новыми данными и учётными данными пользователя
            CommandRequest request = new CommandRequest(
                    CommandType.UPDATE,
                    new String[]{String.valueOf(id)},
                    city,
                    login,
                    password
            );

            System.out.println("Отправка запроса на обновление...");
            CommandResponse response = networkManager.sendRequest(socket, request, host, port);

            return response != null ? response : new CommandResponse(false, "Сервер не ответил");

        } catch (Exception e) {
            return new CommandResponse(false, "Ошибка отправки запроса: " + e.getMessage());
        }
    }
}
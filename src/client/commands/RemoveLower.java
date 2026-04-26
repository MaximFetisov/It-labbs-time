package client.commands;

import client.forms.CityForm;
import client.managers.NetworkManager;
import common.CommandRequest;
import common.CommandResponse;
import common.CommandType;
import common.City;

import java.net.DatagramSocket;

/**
 * Клиентская команда RemoveLower.
 * Отправляет запрос на сервер для удаления элементов, меньших заданного.
 *
 * @author Максим
 * @see NetworkManager
 * @see CityForm
 */
public class RemoveLower {

    /**
     * Выполняет команду удаления элементов, меньших заданного.
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
            System.out.println("Введите элемент для сравнения (будет создан новый объект City):");
            CityForm cityForm = new CityForm();
            City city = cityForm.build();

            if (city == null) {
                return new CommandResponse(false,
                        "Ошибка при создании объекта City");
            }

            CommandRequest request = new CommandRequest(
                    CommandType.REMOVE_LOWER,
                    new String[0],
                    city
            );

            System.out.println("Отправка запроса на сервер...");
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
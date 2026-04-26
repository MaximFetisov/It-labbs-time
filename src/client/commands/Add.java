package client.commands;

import client.forms.CityForm;
import client.managers.NetworkManager;
import common.CommandRequest;
import common.CommandResponse;
import common.CommandType;
import common.City;

import java.net.DatagramSocket;

/**
 * Клиентская команда для добавления нового элемента в коллекцию.
 * Не выполняет логику добавления, только:
 * <ul>
 *   <li>Создаёт объект City через CityForm</li>
 *   <li>Формирует CommandRequest</li>
 *   <li>Отправляет запрос на сервер</li>
 *   <li>Получает и возвращает ответ</li>
 * </ul>
 *
 * @author Максим
 * @see CityForm
 * @see NetworkManager
 */
public class Add {

    /**
     * Выполняет команду добавления элемента.
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
            System.out.println("Введите данные нового города:");
            CityForm cityForm = new CityForm();
            City city = cityForm.build();

            if (city == null) {
                return new CommandResponse(false,
                        "Ошибка при создании объекта City");
            }

            CommandRequest request = new CommandRequest(
                    CommandType.ADD,
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
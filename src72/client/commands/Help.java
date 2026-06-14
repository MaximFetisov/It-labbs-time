package client.commands;

import client.managers.NetworkManager;
import common.CommandRequest;
import common.CommandResponse;
import common.CommandType;

import java.net.DatagramSocket;

/**
 * Клиентская команда для вывода справки по всем доступным командам.
 * Отправляет запрос на сервер для получения списка команд.
 */
public class Help {

    /**
     * Выполняет команду вывода справки.
     *
     * @param socket сокет для отправки запроса
     * @param networkManager менеджер сети
     * @param host хост сервера
     * @param port порт сервера
     * @param login логин пользователя для авторизации
     * @param password пароль пользователя для авторизации
     * @return результат выполнения команды
     */
    public CommandResponse execute(DatagramSocket socket,
                                   NetworkManager networkManager,
                                   String host,
                                   int port,
                                   String login,
                                   String password) {
        try {
            CommandRequest request = new CommandRequest(
                    CommandType.HELP,
                    new String[0],
                    null,
                    login,
                    password
            );

            System.out.println("Запрос справки...");
            CommandResponse response = networkManager.sendRequest(socket, request, host, port);

            return response != null ? response :
                    new CommandResponse(false, "Сервер не ответил");

        } catch (Exception e) {
            return new CommandResponse(false,
                    "Ошибка отправки запроса: " + e.getMessage());
        }
    }

    /**
     * Возвращает локальную справку (без запроса к серверу).
     * Используется при недоступности сервера.
     *
     * @return строка со справкой
     */
    public String getLocalHelp() {
        StringBuilder helpText = new StringBuilder();
        helpText.append("=== Справка по доступным командам (Клиент) ===\n");
        helpText.append("help : Вывести эту справку\n");
        helpText.append("info : Вывести информацию о коллекции\n");
        helpText.append("show : Вывести все элементы коллекции\n");
        helpText.append("clear : Очистить коллекцию (ваши элементы)\n");
        helpText.append("add : Добавить новый элемент\n");
        helpText.append("update : Обновить элемент по ID\n");
        helpText.append("remove_by_id : Удалить элемент по ID\n");
        helpText.append("remove_first : Удалить первый элемент (ваш)\n");
        helpText.append("remove_lower : Удалить элементы меньшие заданного (ваши)\n");
        helpText.append("history : Показать историю команд\n");
        helpText.append("sum_of_meters_above_sea_level : Сумма метров над уровнем моря\n");
        helpText.append("min_by_coordinates : Элемент с минимальными координатами\n");
        helpText.append("print_unique_governor : Уникальные губернаторы\n");
        helpText.append("execute_script : Выполнить скрипт из файла\n");
        helpText.append("login : Войти в аккаунт\n");
        helpText.append("register : Зарегистрироваться\n");
        helpText.append("logout : Выйти из аккаунта\n");
        helpText.append("exit : Завершить работу клиента\n");
        helpText.append("never : Пасхалка\n");
        helpText.append("==============================================\n");
        helpText.append("Примечание: команда 'save' больше не требуется (данные в БД)\n");
        helpText.append("Примечание: модификация возможна только для ваших элементов");

        return helpText.toString();
    }
}
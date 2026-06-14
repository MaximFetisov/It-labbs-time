package client.managers;

import client.forms.CityForm;
import common.CommandRequest;
import common.CommandType;
import common.City;

/**
 * Менеджер для чтения и обработки пользовательского ввода.
 */
public class InputManager {
    private CityForm cityForm;

    public InputManager() {
        this.cityForm = new CityForm();
    }

    /**
     * Создаёт запрос команды с авторизацией.
     */
    public CommandRequest createRequest(CommandType type, String args, String login, String password) {
        try {
            String[] arguments = args.trim().isEmpty() ? new String[0] : args.trim().split("\\s+");

            if (type == CommandType.ADD) {
                System.out.println("Введите данные нового города:");
                City city = cityForm.build();
                if (city == null) {
                    System.err.println("Ошибка при создании объекта City");
                    return null;
                }
                return new CommandRequest(type, arguments, city, login, password);
            }

            if (type == CommandType.UPDATE) {
                if (arguments.length == 0) {
                    System.err.println("Ошибка: для команды update требуется ID");
                    return null;
                }
                System.out.println("Введите данные для обновления города:");
                City city = cityForm.buildForUpdate();
                if (city == null) {
                    System.err.println("Ошибка при создании объекта City");
                    return null;
                }
                return new CommandRequest(type, arguments, city, login, password);
            }

            if (type == CommandType.REMOVE_LOWER) {
                System.out.println("Введите город для сравнения:");
                City city = cityForm.build();
                if (city == null) {
                    System.err.println("Ошибка при создании объекта City");
                    return null;
                }
                return new CommandRequest(type, arguments, city, login, password);
            }

            return new CommandRequest(type, arguments, null, login, password);

        } catch (Exception e) {
            System.err.println("Ошибка при создании запроса: " + e.getMessage());
            return null;
        }
    }

    public CityForm getCityForm() {
        return cityForm;
    }
}
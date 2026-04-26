package client.managers;

import client.forms.CityForm;
import common.CommandRequest;
import common.CommandType;
import common.City;

/**
 * Менеджер для чтения и обработки пользовательского ввода.
 * Создаёт объекты {@link CommandRequest} на основе введённых команд
 * и передаёт их для отправки на сервер.
 * <p>
 * Не использует статические классы из Lab 5 (Run, ChackValues и т.д.).
 * Все формы ввода создаются локально.
 * </p>
 *
 * @author Максим
 * @see CommandRequest
 * @see CommandType
 * @see CityForm
 */
public class InputManager {
    private CityForm cityForm;

    /**
     * Создаёт новый InputManager с инициализацией формы ввода города.
     */
    public InputManager() {
        this.cityForm = new CityForm();
    }

    /**
     *
     *
     * Создаёт запрос команды на основе типа и аргументов.
     * <p>
     * Для команд, требующих объект City (ADD, UPDATE, REMOVE_LOWER),
     * вызывает интерактивный ввод через CityForm.
     * </p>
     *
     * @param type тип команды
     * @param args аргументы команды (строка)
     * @return CommandRequest или null при ошибке ввода
     */
    public CommandRequest createRequest(CommandType type, String args) {
        try {
            String[] arguments = args.trim().isEmpty() ? new String[0] : args.trim().split("\\s+");

            if (type == CommandType.ADD) {
                System.out.println("Введите данные нового города:");
                City city = cityForm.build();
                if (city == null) {
                    System.err.println("Ошибка при создании объекта City");
                    return null;
                }
                return new CommandRequest(type, arguments, city);
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
                return new CommandRequest(type, arguments, city);
            }


            if (type == CommandType.REMOVE_LOWER) {
                System.out.println("Введите город для сравнения:");
                City city = cityForm.build();
                if (city == null) {
                    System.err.println("Ошибка при создании объекта City");
                    return null;
                }
                return new CommandRequest(type, arguments, city);
            }



            return new CommandRequest(type, arguments, null);

        } catch (Exception e) {
            System.err.println("Ошибка при создании запроса: " + e.getMessage());
            return null;
        }
    }

    /**
     * Возвращает экземпляр CityForm для тестирования или расширенного использования.
     *
     * @return форма ввода города
     */
    public CityForm getCityForm() {
        return cityForm;
    }

}
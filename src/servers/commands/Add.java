package servers.commands;

import common.CommandRequest;
import common.CommandResponse;
import common.City;
import servers.interfacess.Command;
import servers.managers.CollectionManager;
import servers.managers.FileManager;

/**
 * Команда добавления нового элемента в коллекцию.
 * Выполняется на сервере. Получает объект City от клиента,
 * генерирует уникальный ID и добавляет в коллекцию.
 * <p>
 * Использует Stream API для операций с коллекцией.
 * </p>
 *
 * @author Максим
 * @see Command
 * @see City
 * @see CollectionManager
 */
public class Add implements Command {

    /**
     * Выполняет команду добавления элемента в коллекцию.
     *
     * @param request запрос от клиента (содержит объект City)
     * @param collectionManager менеджер коллекции
     * @param fileManager менеджер файлов (не используется для add)
     * @param dataFile путь к файлу данных (не используется для add)
     * @return результат выполнения команды
     */
    @Override
    public CommandResponse execute(CommandRequest request,
                                   CollectionManager collectionManager,
                                   FileManager fileManager,
                                   String dataFile) {
        try {
            City city = request.getCity();
            if (city == null) {
                return new CommandResponse(false,
                        "Ошибка: объект City не передан в запросе");
            }

            if (city.getName() == null || city.getName().trim().isEmpty()) {
                return new CommandResponse(false,
                        "Ошибка: название города не может быть пустым");
            }

            if (city.getCoordinates() == null) {
                return new CommandResponse(false,
                        "Ошибка: координаты не могут быть null");
            }

            if (city.getArea() == null || city.getArea() <= 0) {
                return new CommandResponse(false,
                        "Ошибка: площадь должна быть больше 0");
            }

            if (city.getPopulation() <= 0) {
                return new CommandResponse(false,
                        "Ошибка: население должно быть больше 0");
            }

            long generatedId = collectionManager.generateId();
            city.setId(generatedId);

            collectionManager.add(city);

            return new CommandResponse(true,
                    "Элемент успешно добавлен с ID: " + generatedId);

        } catch (Exception e) {
            return new CommandResponse(false,
                    "Ошибка при добавлении элемента: " + e.getMessage());
        }
    }

    @Override
    public String getDescription() {
        return "Добавляет новый элемент в коллекцию";
    }

    @Override
    public String getName() {
        return "add";
    }
}
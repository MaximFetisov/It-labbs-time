package servers.commands;

import common.CommandRequest;
import common.CommandResponse;
import common.City;
import servers.interfacess.Command;
import servers.managers.CollectionManager;
import servers.managers.FileManager;

/**
 * Команда для обновления элемента коллекции по его ID.
 * Получает объект City из запроса и обновляет существующий элемент
 * с тем же ID, сохраняя уникальность идентификатора.
 */
public class Update implements Command {

    @Override
    public CommandResponse execute(CommandRequest request,
                                   CollectionManager collectionManager,
                                   FileManager fileManager,
                                   String dataFile) {
        try {
            // Проверка на аргументы (ID обязателен)
            if (!request.hasArguments()) {
                return new CommandResponse(false,
                        "Ошибка: не указан ID элемента для обновления");
            }

            // Парсинг ID из аргументов
            long id;
            try {
                id = Long.parseLong(request.getFirstArgument());
            } catch (NumberFormatException e) {
                return new CommandResponse(false,
                        "Ошибка: ID должен быть числом типа long");
            }

            // Проверка наличия объекта City в запросе
            City newCity = request.getCity();
            if (newCity == null) {
                return new CommandResponse(false,
                        "Ошибка: не переданы данные для обновления");
            }

            // Проверка на пустую коллекцию
            if (collectionManager.getSize() == 0) {
                return new CommandResponse(false,
                        "Коллекция пуста. Элемент с таким ID не существует");
            }

            // Проверка существования элемента с таким ID
            boolean exists = collectionManager.getCities().stream()
                    .anyMatch(city -> city.getId() == id);

            if (!exists) {
                // ПОНЯТНОЕ СООБЩЕНИЕ ДЛЯ ПОЛЬЗОВАТЕЛЯ
                return new CommandResponse(false,
                        "Элемент с ID " + id + " не найден. " +
                                "Проверьте ID командой 'show' и попробуйте снова");
            }

            // Устанавливаем правильный ID (чтобы нельзя было изменить ID)
            newCity.setId(id);

            // Обновление элемента в коллекции
            boolean updated = collectionManager.update(id, newCity);

            if (updated) {
                return new CommandResponse(true,
                        "Элемент с ID " + id + " успешно обновлён");
            } else {
                return new CommandResponse(false,
                        "Не удалось обновить элемент. Попробуйте снова");
            }

        } catch (Exception e) {
            // Логирование для разработчика, пользователю — понятное сообщение
            java.util.logging.Logger.getLogger(Update.class.getName())
                    .severe("Ошибка при обновлении: " + e.getMessage());

            return new CommandResponse(false,
                    "Произошла ошибка при обновлении элемента");
        }
    }

    @Override
    public String getDescription() {
        return "Обновить значение элемента коллекции, ID которого соответствует заданному";
    }

    @Override
    public String getName() {
        return "update";
    }
}
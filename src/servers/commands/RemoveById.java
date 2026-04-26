package servers.commands;

import common.CommandRequest;
import common.CommandResponse;
import servers.interfacess.Command;
import servers.managers.CollectionManager;
import servers.managers.FileManager;

/**
 * Команда для удаления элемента из коллекции по его ID.
 * Использует Stream API для поиска и удаления элемента (требование Lab 6).
 * <p>
 * Если элемент с указанным ID не найден, выводит соответствующее сообщение.
 * </p>
 *
 * @author Максим
 * @see Command
 * @see CollectionManager
 */
public class RemoveById implements Command {

    /**
     * Выполняет команду удаления элемента по ID.
     *
     * @param request запрос от клиента (содержит ID в аргументах)
     * @param collectionManager менеджер коллекции
     * @param fileManager менеджер файлов (не используется для этой команды)
     * @param dataFile путь к файлу данных (не используется для этой команды)
     * @return результат выполнения команды
     */
    @Override
    public CommandResponse execute(CommandRequest request,
                                   CollectionManager collectionManager,
                                   FileManager fileManager,
                                   String dataFile) {
        try {
            // Проверка на аргументы (ID обязателен)
            if (!request.hasArguments()) {
                return new CommandResponse(false,
                        "Ошибка: не указан ID элемента для удаления");
            }

            long id;
            try {
                id = Long.parseLong(request.getFirstArgument());
            } catch (NumberFormatException e) {
                return new CommandResponse(false,
                        "Ошибка: ID должен быть числом типа long");
            }

            if (collectionManager.getSize() == 0) {
                return new CommandResponse(true,
                        "Коллекция пуста. Нечего удалять.");
            }

            boolean exists = collectionManager.getCities().stream()
                    .anyMatch(city -> city.getId() == id);

            if (!exists) {
                return new CommandResponse(false,
                        "Элемент с ID " + id + " не найден в коллекции");
            }

            boolean removed = collectionManager.removeById(id);

            if (removed) {
                return new CommandResponse(true,
                        "Элемент с ID " + id + " успешно удалён из коллекции");
            } else {
                return new CommandResponse(false,
                        "Не удалось удалить элемент с ID " + id);
            }

        } catch (Exception e) {
            return new CommandResponse(false,
                    "Ошибка при удалении элемента: " + e.getMessage());
        }
    }

    @Override
    public String getDescription() {
        return "Удалить элемент из коллекции по его ID";
    }

    @Override
    public String getName() {
        return "remove_by_id";
    }
}
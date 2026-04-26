package servers.commands;

import common.CommandRequest;
import common.CommandResponse;
import common.City;
import servers.interfacess.Command;
import servers.managers.CollectionManager;
import servers.managers.FileManager;

/**
 * Команда для удаления первого элемента из коллекции.
 * Удаляет элемент, который находится первым в отсортированной коллекции
 * (согласно методу {@link City#compareTo(City)}).
 * <p>
 * Использует Stream API для получения первого элемента (требование Lab 6).
 * </p>
 *
 * @author Максим
 * @see Command
 * @see City
 * @see CollectionManager
 */
public class RemoveFirst implements Command {

    /**
     * Выполняет команду удаления первого элемента коллекции.
     *
     * @param request запрос от клиента
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
            if (request.hasArguments()) {
                return new CommandResponse(false,
                        "У этой команды отсутствуют параметры");
            }

            if (collectionManager.getSize() == 0) {
                return new CommandResponse(true,
                        "Коллекция пуста. Нечего удалять.");
            }

            City firstCity = collectionManager.getCities().stream()
                    .findFirst()
                    .orElse(null);

            if (firstCity == null) {
                return new CommandResponse(false,
                        "Не удалось получить первый элемент коллекции.");
            }

            long removedId = firstCity.getId();
            String removedName = firstCity.getName();

            collectionManager.removeFirst();

            StringBuilder message = new StringBuilder();
            message.append("=== Удаление первого элемента ===\n");
            message.append("Удалён элемент:\n");
            message.append("  ID: ").append(removedId).append("\n");
            message.append("  Название: ").append(removedName).append("\n");
            message.append("Осталось элементов: ").append(collectionManager.getSize()).append("\n");
            message.append("================================");

            return new CommandResponse(true, message.toString());

        } catch (Exception e) {
            return new CommandResponse(false,
                    "Ошибка при удалении первого элемента: " + e.getMessage());
        }
    }

    @Override
    public String getDescription() {
        return "Удалить первый элемент из коллекции";
    }

    @Override
    public String getName() {
        return "remove_first";
    }
}
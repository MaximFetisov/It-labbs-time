package servers.commands;

import common.CommandRequest;
import common.CommandResponse;
import common.City;
import servers.interfacess.Command;
import servers.managers.CollectionManager;
import servers.managers.FileManager;

/**
 * Команда для удаления всех элементов, которые меньше заданного.
 * Создаёт новый объект City через данные из запроса,
 * затем удаляет все элементы коллекции, которые меньше этого эталона
 * (согласно методу {@link City#compareTo(City)}).
 * <p>
 * Использует Stream API для удаления элементов (требование Lab 6).
 * </p>
 *
 * @author Максим
 * @see Command
 * @see City
 * @see CollectionManager
 */
public class RemoveLower implements Command {

    /**
     * Выполняет команду удаления элементов, меньших заданного.
     *
     * @param request запрос от клиента (содержит объект City для сравнения)
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


            City referenceCity = request.getCity();
            if (referenceCity == null) {
                return new CommandResponse(false,
                        "Ошибка: объект City для сравнения не передан");
            }

            if (collectionManager.getSize() == 0) {
                return new CommandResponse(true,
                        "Коллекция пуста. Нечего удалять.");
            }


            int initialSize = collectionManager.getSize();

            int removedCount = collectionManager.removeLower(referenceCity);

            StringBuilder message = new StringBuilder();
            message.append("=== Удаление элементов, меньших заданного ===\n");
            message.append("Элементов было: ").append(initialSize).append("\n");
            message.append("Элементов удалено: ").append(removedCount).append("\n");
            message.append("Элементов осталось: ").append(collectionManager.getSize()).append("\n");
            message.append("===============================================");

            return new CommandResponse(true, message.toString());

        } catch (Exception e) {
            return new CommandResponse(false,
                    "Ошибка при выполнении команды: " + e.getMessage());
        }
    }

    @Override
    public String getDescription() {
        return "Удалить из коллекции все элементы, меньшие, чем заданный";
    }

    @Override
    public String getName() {
        return "remove_lower";
    }
}
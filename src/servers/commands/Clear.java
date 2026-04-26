package servers.commands;

import common.CommandRequest;
import common.CommandResponse;
import servers.interfacess.Command;
import servers.managers.CollectionManager;
import servers.managers.FileManager;

/**
 * Команда для очистки коллекции.
 * Удаляет все элементы из коллекции на сервере.
 * <p>
 * Использует Stream API для операций с коллекцией.
 * </p>
 *
 * @author Максим
 * @see Command
 * @see CollectionManager
 */
public class Clear implements Command {

    /**
     * Выполняет команду очистки коллекции.
     *
     * @param request запрос от клиента
     * @param collectionManager менеджер коллекции
     * @param fileManager менеджер файлов (не используется для clear)
     * @param dataFile путь к файлу данных (не используется для clear)
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
                        "Коллекция уже пуста. Нечего очищать.");
            }

            int sizeBefore = collectionManager.getSize();

            collectionManager.clear();

            return new CommandResponse(true,
                    "Коллекция успешно очищена. Удалено элементов: " + sizeBefore);

        } catch (Exception e) {
            return new CommandResponse(false,
                    "Ошибка при очистке коллекции: " + e.getMessage());
        }
    }

    @Override
    public String getDescription() {
        return "Очищает коллекцию";
    }

    @Override
    public String getName() {
        return "clear";
    }
}
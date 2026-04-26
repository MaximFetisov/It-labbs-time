package servers.interfacess;

import common.CommandRequest;
import common.CommandResponse;
import servers.managers.CollectionManager;
import servers.managers.FileManager;

/**
 * Базовый интерфейс для всех команд сервера.
 * Все конкретные команды должны реализовывать этот интерфейс.
 *
 * @author Максим
 * @see CommandRequest
 * @see CommandResponse
 */
public interface Command {

    /**
     * Выполняет команду с указанным запросом.
     *
     * @param request запрос от клиента (содержит тип, аргументы, данные)
     * @param collectionManager менеджер коллекции
     * @param fileManager менеджер файлов (для save)
     * @param dataFile путь к файлу данных
     * @return результат выполнения команды
     */
    CommandResponse execute(CommandRequest request,
                            CollectionManager collectionManager,
                            FileManager fileManager,
                            String dataFile);

    /**
     * Возвращает описание команды для справки.
     *
     * @return описание команды
     */
    default String getDescription() {
        return "Нет описания";
    }

    /**
     * Возвращает имя команды.
     *
     * @return имя команды
     */
    default String getName() {
        return "unknown";
    }
}
package servers.commands;

import common.CommandRequest;
import common.CommandResponse;
import servers.interfacess.Command;
import servers.managers.CollectionManager;
import servers.managers.FileManager;

import java.util.logging.Logger;

/**
 * Команда для сохранения коллекции в файл.
 * Доступна ТОЛЬКО на сервере (требование Lab 6).
 * <p>
 * Сохраняет текущее состояние коллекции в формате JSON.
 * </p>
 *
 * @author Максим
 * @see Command
 * @see FileManager
 */
public class Save implements Command {
    private static final Logger logger = Logger.getLogger(Save.class.getName());

    /**
     * Выполняет команду сохранения коллекции в файл.
     *
     * @param request запрос от клиента
     * @param collectionManager менеджер коллекции
     * @param fileManager менеджер файлов
     * @param dataFile путь к файлу данных
     * @return результат выполнения команды
     */
    @Override
    public CommandResponse execute(CommandRequest request,
                                   CollectionManager collectionManager,
                                   FileManager fileManager,
                                   String dataFile) {
        try {
            logger.info("Начало сохранения коллекции в файл: " + dataFile);

            // Сохранение в JSON через Gson
            fileManager.writeToJsonFile(dataFile, collectionManager.getCities());

            logger.info("Коллекция успешно сохранена");
            return new CommandResponse(true,
                    "Коллекция успешно сохранена в файл: " + dataFile);

        } catch (Exception e) {
            logger.severe("Ошибка при сохранении коллекции: " + e.getMessage());
            return new CommandResponse(false,
                    "Ошибка при сохранении: " + e.getMessage());
        }
    }

    @Override
    public String getDescription() {
        return "Сохранить коллекцию в файл (доступно только серверу)";
    }

    @Override
    public String getName() {
        return "save";
    }
}
package servers.modules;

import common.CommandRequest;
import common.CommandResponse;
import common.CommandType;
import servers.managers.CollectionManager;
import servers.managers.FileManager;
import servers.commands.*;
import java.util.logging.Logger;

/**
 * Модуль обработки полученных команд.
 * Маршрутизирует запросы к соответствующим обработчикам команд.
 *
 * @author Максим
 * @see CommandRequest
 * @see CommandResponse
 */
public class CommandModule {
    private static final Logger logger = Logger.getLogger(CommandModule.class.getName());

    private CollectionManager collectionManager;
    private FileManager fileManager;
    private String dataFile;

    /**
     * Создаёт модуль обработки команд.
     *
     * @param collectionManager менеджер коллекции
     * @param fileManager менеджер файлов
     * @param dataFile путь к файлу данных
     */
    public CommandModule(CollectionManager collectionManager, FileManager fileManager, String dataFile) {
        this.collectionManager = collectionManager;
        this.fileManager = fileManager;
        this.dataFile = dataFile;
    }

    /**
     * Выполняет команду на основе типа запроса.
     *
     * @param request запрос от клиента
     * @return результат выполнения команды
     */
    public CommandResponse execute(CommandRequest request) {
        logger.info("Выполнение команды: " + request.getType());

        try {
             switch (request.getType()) {
                 case HELP:
                     return new Help().execute(request, collectionManager, fileManager, dataFile);
                 case INFO:
                     return new Info().execute(request, collectionManager, fileManager, dataFile);

                 case SHOW:
                     return new Show().execute(request, collectionManager, fileManager, dataFile);
                 case CLEAR:
                    return new Clear().execute(request, collectionManager, fileManager, dataFile);
                    case ADD:
                        return new Add().execute(request, collectionManager, fileManager, dataFile);
                 case SUM_OF_METERS_ABOVE_SEA_LEVEL:
                     return new SumOfMeters().execute(request, collectionManager, fileManager, dataFile);
                 case HISTORY:
                     return new History().execute(request, collectionManager, fileManager, dataFile);
                 case MIN_BY_COORDINATES:
                     return new MinByCoordinates().execute(request, collectionManager, fileManager, dataFile);
                 case PRINT_UNIQUE_GOVERNOR:
                     return new PrintUniqueGovernor().execute(request, collectionManager, fileManager, dataFile);
                 case REMOVE_LOWER:
                    return new RemoveLower().execute(request, collectionManager, fileManager, dataFile);
                 case REMOVE_FIRST:
                     return new RemoveFirst().execute(request, collectionManager, fileManager, dataFile);
                 case REMOVE_BY_ID:
                     return new RemoveById().execute(request, collectionManager, fileManager, dataFile);
                 case UPDATE:
                     return new Update().execute(request, collectionManager, fileManager, dataFile);
                 case EXECUTE_SCRIPT:
                    return new ExecuteScript().execute(request, collectionManager, fileManager, dataFile);
                 case SAVE:
                    return new Save().execute(request, collectionManager, fileManager, dataFile);

                default:
                     return new CommandResponse(false, "Неизвестная команда: " + request.getType());
            }
         } catch (Exception e) {
             logger.severe("Ошибка выполнения команды: " + e.getMessage());
             return new CommandResponse(false, "Ошибка: " + e.getMessage());
        }
   }
}
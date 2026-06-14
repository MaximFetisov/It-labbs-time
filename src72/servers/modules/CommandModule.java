package servers.modules;

import common.CommandRequest;
import common.CommandResponse;
import common.CommandType;
import servers.managers.CollectionManager;
import servers.managers.DatabaseManager;
import servers.commands.*;
import java.util.logging.Logger;

/**
 * Модуль обработки полученных команд для Lab 7.
 * Маршрутизирует запросы к соответствующим обработчикам команд.
 * <p>
 * Требования Lab 7:
 * <ul>
 *   <li>Проверка авторизации перед выполнением команд</li>
 *   <li>Использование DatabaseManager вместо FileManager</li>
 *   <li>Передача логина пользователя в команды для проверки прав</li>
 * </ul>
 * </p>
 *
 * @author Максим
 * @see CommandRequest
 * @see CommandResponse
 * @see DatabaseManager
 */
public class CommandModule {
    private static final Logger logger = Logger.getLogger(CommandModule.class.getName());

    private CollectionManager collectionManager;
    private DatabaseManager databaseManager;

    /**
     * Создаёт модуль обработки команд.
     *
     * @param collectionManager менеджер коллекции (кэш в памяти)
     * @param databaseManager менеджер базы данных
     */
    public CommandModule(CollectionManager collectionManager, DatabaseManager databaseManager) {
        this.collectionManager = collectionManager;
        this.databaseManager = databaseManager;
    }

    /**
     * Выполняет команду на основе типа запроса.
     * <p>
     * Перед выполнением проверяет авторизацию пользователя
     * (кроме команд login/register).
     * </p>
     *
     * @param request запрос от клиента (содержит login/password)
     * @return результат выполнения команды
     */
    public CommandResponse execute(CommandRequest request) {
        logger.info("Выполнение команды: " + request.getType());

        try {
            switch (request.getType()) {
                case LOGIN:
                    return new Login().execute(request, collectionManager, databaseManager);
                case REGISTER:
                    return new Register().execute(request, collectionManager, databaseManager);
                case LOGOUT:
                    return new Logout().execute(request, collectionManager, databaseManager);
            }

            //  ПРОВЕРКА АВТОРИЗАЦИИ
            if (request.getType() != CommandType.LOGIN && request.getType() != CommandType.REGISTER) {
                String login = request.getLogin();
                String password = request.getPassword();

                if (login == null || password == null || login.isEmpty() || password.isEmpty()) {
                    return new CommandResponse(false,
                            "Ошибка авторизации: логин и пароль обязательны");
                }

                if (!databaseManager.checkUserPassword(login, password)) {
                    logger.warning("Неудачная авторизация для пользователя: " + login);
                    return new CommandResponse(false,
                            "Ошибка авторизации: неверный логин или пароль", 9000);
                }

                logger.info("Авторизация успешна для пользователя: " + login);
            }


            //  МАРШРУТИЗАЦИЯ КОМАНД
            switch (request.getType()) {
                case HELP:
                    return new Help().execute(request, collectionManager, databaseManager);
                case INFO:
                    return new Info().execute(request, collectionManager, databaseManager);
                case SHOW:
                    return new Show().execute(request, collectionManager, databaseManager);
                case CLEAR:
                    return new Clear().execute(request, collectionManager, databaseManager);
                case ADD:
                    return new Add().execute(request, collectionManager, databaseManager);
                case SUM_OF_METERS_ABOVE_SEA_LEVEL:
                    return new SumOfMeters().execute(request, collectionManager, databaseManager);
                case HISTORY:
                    return new History().execute(request, collectionManager, databaseManager);
                case MIN_BY_COORDINATES:
                    return new MinByCoordinates().execute(request, collectionManager, databaseManager);
                case PRINT_UNIQUE_GOVERNOR:
                    return new PrintUniqueGovernor().execute(request, collectionManager, databaseManager);
                case REMOVE_LOWER:
                    return new RemoveLower().execute(request, collectionManager, databaseManager);
                case REMOVE_FIRST:
                    return new RemoveFirst().execute(request, collectionManager, databaseManager);
                case REMOVE_BY_ID:
                    return new RemoveById().execute(request, collectionManager, databaseManager);
                case UPDATE:
                    return new Update().execute(request, collectionManager, databaseManager);
                case EXECUTE_SCRIPT:
                    return new ExecuteScript().execute(request, collectionManager, databaseManager);

                case SAVE:
                    return new CommandResponse(false,
                            "Команда 'save' больше не требуется: данные автоматически сохраняются в БД)");

                default:
                    return new CommandResponse(false,
                            "Неизвестная команда: " + request.getType());
            }

        } catch (Exception e) {
            logger.severe("Ошибка выполнения команды: " + e.getMessage());
            return new CommandResponse(false,
                    "Внутренняя ошибка сервера: " + e.getMessage());
        }
    }
}
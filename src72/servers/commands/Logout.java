package servers.commands;

import common.CommandRequest;
import common.CommandResponse;
import servers.interfacess.Command;
import servers.managers.CollectionManager;
import servers.managers.DatabaseManager;

/**
 * Команда выхода из аккаунта.
 * <p>
 * Так как авторизация проверяется по каждому запросу (логин/пароль в CommandRequest),
 * сервер не хранит сессию пользователя. Поэтому эта команда — заглушка,
 * которая просто подтверждает "выход" для клиента.
 * </p>
 */
public class Logout implements Command {

    @Override
    public CommandResponse execute(CommandRequest request,
                                   CollectionManager collectionManager,
                                   DatabaseManager databaseManager) {
        // Сервер не хранит сессии, поэтому просто возвращаем успех
        // Клиент сам очистит сохранённые логин/пароль
        return new CommandResponse(true, "Вы вышли из аккаунта");
    }

    @Override
    public String getDescription() {
        return "Выйти из аккаунта";
    }

    @Override
    public String getName() {
        return "logout";
    }
}
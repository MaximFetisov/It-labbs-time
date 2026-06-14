package servers.commands;

import common.CommandRequest;
import common.CommandResponse;
import servers.interfacess.Command;
import servers.managers.CollectionManager;
import servers.managers.DatabaseManager;

/**
 * Команда входа в аккаунт.
 * Проверяет логин и пароль пользователя в базе данных.
 */
public class Login implements Command {

    @Override
    public CommandResponse execute(CommandRequest request,
                                   CollectionManager collectionManager,
                                   DatabaseManager databaseManager) {
        String login = request.getLogin();
        String password = request.getPassword();

        if (login == null || login.trim().isEmpty() || password == null || password.isEmpty()) {
            return new CommandResponse(false, "Ошибка: логин и пароль не могут быть пустыми");
        }

        // Проверка пароля через базу данных
        if (databaseManager.checkUserPassword(login.trim(), password)) {
            return new CommandResponse(true, "Успешный вход в аккаунт: " + login.trim());
        } else {
            return new CommandResponse(false, "Ошибка: неверный логин или пароль");
        }
    }

    @Override
    public String getDescription() {
        return "Войти в аккаунт";
    }

    @Override
    public String getName() {
        return "login";
    }
}
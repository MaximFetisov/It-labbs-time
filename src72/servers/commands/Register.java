package servers.commands;

import common.CommandRequest;
import common.CommandResponse;
import servers.interfacess.Command;
import servers.managers.CollectionManager;
import servers.managers.DatabaseManager;

/**
 * Команда регистрации нового пользователя.
 * Создаёт запись в таблице users с хешированным паролем.
 */
public class Register implements Command {

    @Override
    public CommandResponse execute(CommandRequest request,
                                   CollectionManager collectionManager,
                                   DatabaseManager databaseManager) {
        String login = request.getLogin();
        String password = request.getPassword();

        if (login == null || login.trim().isEmpty() || password == null || password.isEmpty()) {
            return new CommandResponse(false, "Ошибка: логин и пароль не могут быть пустыми");
        }

        login = login.trim();

        if (login.length() < 4) {
            return new CommandResponse(false, "Ошибка: логин должен содержать не менее 4 символов");
        }
        if (password.length() < 6) {
            return new CommandResponse(false, "Ошибка: пароль должен содержать не менее 6 символов");
        }

        // Регистрация через базу данных
        if (databaseManager.register(login, password)) {
            return new CommandResponse(true, "Пользователь '" + login + "' успешно зарегистрирован");
        } else {
            return new CommandResponse(false, "Ошибка: пользователь с таким логином уже существует");
        }
    }

    @Override
    public String getDescription() {
        return "Зарегистрировать нового пользователя";
    }

    @Override
    public String getName() {
        return "register";
    }
}
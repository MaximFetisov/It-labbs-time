package servers.commands;

import common.CommandRequest;
import common.CommandResponse;
import servers.interfacess.Command;
import servers.managers.CollectionManager;
import servers.managers.DatabaseManager;

import java.util.logging.Logger;

public class Clear implements Command {
    private static final Logger logger = Logger.getLogger(Clear.class.getName());

    @Override
    public CommandResponse execute(CommandRequest request,
                                   CollectionManager collectionManager,
                                   DatabaseManager databaseManager) {
        try {
            if (request.hasArguments()) {
                return new CommandResponse(false, "У этой команды отсутствуют параметры");
            }

            String author = request.getLogin();
            if (author == null || author.isEmpty()) {
                return new CommandResponse(false, "Ошибка авторизации.");
            }

            int deletedCount = collectionManager.clearOwn(author);

            logger.info("Пользователь '" + author + "' очистил свою часть коллекции. Удалено: " + deletedCount);
            return new CommandResponse(true,
                    "Ваша часть коллекции очищена. Удалено элементов: " + deletedCount);

        } catch (Exception e) {
            logger.severe("Ошибка выполнения команды clear: " + e.getMessage());
            e.printStackTrace();
            return new CommandResponse(false, "Произошла внутренняя ошибка сервера.");
        }
    }

    @Override
    public String getName() {
        return "clear";
    }

    @Override
    public String getDescription() {
        return "Очистить коллекцию (только ваши элементы)";
    }
}
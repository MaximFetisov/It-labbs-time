package servers.commands;

import common.CommandRequest;
import common.CommandResponse;
import common.City;
import servers.interfacess.Command;
import servers.managers.CollectionManager;
import servers.managers.DatabaseManager;

import java.util.logging.Logger;

public class RemoveLower implements Command {
    private static final Logger logger = Logger.getLogger(RemoveLower.class.getName());

    @Override
    public CommandResponse execute(CommandRequest request,
                                   CollectionManager collectionManager,
                                   DatabaseManager databaseManager) {
        try {
            City referenceCity = request.getCity();
            if (referenceCity == null) {
                return new CommandResponse(false, "Ошибка: объект City для сравнения не передан.");
            }

            String author = request.getLogin();
            if (author == null || author.isEmpty()) {
                return new CommandResponse(false, "Ошибка авторизации.");
            }

            int removedCount = collectionManager.removeLowerOwn(referenceCity, author);

            return new CommandResponse(true, "Удалено элементов: " + removedCount);

        } catch (Exception e) {
            logger.severe("Ошибка выполнения remove_lower: " + e.getMessage());
            e.printStackTrace();
            return new CommandResponse(false, "Внутренняя ошибка сервера.");
        }
    }

    @Override public String getName() { return "remove_lower"; }
    @Override public String getDescription() { return "Удалить ваши элементы, меньшие заданного"; }
}
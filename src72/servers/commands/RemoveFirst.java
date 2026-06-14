package servers.commands;

import common.CommandRequest;
import common.CommandResponse;
import servers.interfacess.Command;
import servers.managers.CollectionManager;
import servers.managers.DatabaseManager;

import java.util.logging.Logger;

public class RemoveFirst implements Command {
    private static final Logger logger = Logger.getLogger(RemoveFirst.class.getName());

    @Override
    public CommandResponse execute(CommandRequest request,
                                   CollectionManager collectionManager,
                                   DatabaseManager databaseManager) {
        try {
            String author = request.getLogin();
            if (author == null || author.isEmpty()) {
                return new CommandResponse(false, "Ошибка авторизации.");
            }

            boolean removed = collectionManager.removeFirstOwn(author);

            if (removed) {

                return new CommandResponse(true, "Первый элемент удалён.");
            } else {
                return new CommandResponse(true, "У вас нет элементов для удаления или коллекция пуста.");
            }
        } catch (Exception e) {
            logger.severe("Ошибка выполнения remove_first: " + e.getMessage());
            e.printStackTrace();
            return new CommandResponse(false, "Внутренняя ошибка сервера.");
        }
    }

    @Override public String getName() { return "remove_first"; }
    @Override public String getDescription() { return "Удалить первый элемент коллекции (ваш минимальный)"; }
}
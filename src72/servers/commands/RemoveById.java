package servers.commands;

import common.CommandRequest;
import common.CommandResponse;
import servers.interfacess.Command;
import servers.managers.CollectionManager;
import servers.managers.DatabaseManager;

import java.util.logging.Logger;

public class RemoveById implements Command {
    private static final Logger logger = Logger.getLogger(RemoveById.class.getName());

    @Override
    public CommandResponse execute(CommandRequest request,
                                   CollectionManager collectionManager,
                                   DatabaseManager databaseManager) {
        try {
            String[] args = request.getArguments();
            if (args == null || args.length == 0) {
                return new CommandResponse(false, "Ошибка: не указан ID элемента для удаления.");
            }

            long id;
            try {
                id = Long.parseLong(args[0]);
            } catch (NumberFormatException e) {
                return new CommandResponse(false, "Ошибка: ID должен быть числом типа long.");
            }

            String author = request.getLogin();

            boolean removed = collectionManager.removeById(id, author);

            if (removed) {
                logger.info("Пользователь '" + author + "' удалил элемент с ID " + id);
                return new CommandResponse(true, "Элемент с ID " + id + " успешно удалён.");
            } else {
                return new CommandResponse(false,
                        "Элемент с ID " + id + " не найден или у вас нет прав на удаление.");
            }

        } catch (Exception e) {
            logger.severe("Ошибка выполнения remove_by_id: " + e.getMessage());
            e.printStackTrace();
            return new CommandResponse(false, "Внутренняя ошибка сервера.");
        }
    }

    @Override
    public String getName() { return "remove_by_id"; }
    @Override
    public String getDescription() { return "Удалить элемент по ID"; }
}
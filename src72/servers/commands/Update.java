package servers.commands;

import common.CommandRequest;
import common.CommandResponse;
import common.City;
import servers.interfacess.Command;
import servers.managers.CollectionManager;
import servers.managers.DatabaseManager;

import java.util.logging.Logger;

public class Update implements Command {
    private static final Logger logger = Logger.getLogger(Update.class.getName());

    @Override
    public CommandResponse execute(CommandRequest request,
                                   CollectionManager collectionManager,
                                   DatabaseManager databaseManager) {
        try {
            String[] args = request.getArguments();
            if (args == null || args.length == 0) {
                return new CommandResponse(false, "Ошибка: не указан ID элемента для обновления.");
            }

            long id;
            try {
                id = Long.parseLong(args[0]);
            } catch (NumberFormatException e) {
                return new CommandResponse(false, "Ошибка: ID должен быть числом типа long.");
            }

            City newCity = request.getCity();
            if (newCity == null) {
                return new CommandResponse(false, "Ошибка: не переданы данные для обновления.");
            }

            String author = request.getLogin();

            boolean updated = collectionManager.updateCity(id, newCity, author);

            if (updated) {
                logger.info("Пользователь '" + author + "' обновил элемент с ID " + id);
                return new CommandResponse(true, "Элемент с ID " + id + " успешно обновлён.");
            } else {
                return new CommandResponse(false,
                        "Элемент с ID " + id + " не найден или у вас нет прав на изменение.");
            }

        } catch (Exception e) {
            logger.severe("Ошибка выполнения update: " + e.getMessage());
            e.printStackTrace();
            return new CommandResponse(false, "Внутренняя ошибка сервера.");
        }
    }

    @Override
    public String getName() { return "update"; }
    @Override
    public String getDescription() { return "Обновить элемент по ID"; }
}
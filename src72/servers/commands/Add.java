package servers.commands;

import common.CommandRequest;
import common.CommandResponse;
import common.City;
import servers.interfacess.Command;
import servers.managers.CollectionManager;
import servers.managers.DatabaseManager;

import java.util.logging.Logger;

public class Add implements Command {
    private static final Logger logger = Logger.getLogger(Add.class.getName());

    @Override
    public CommandResponse execute(CommandRequest request,
                                   CollectionManager collectionManager,
                                   DatabaseManager databaseManager) {
        try {
            City city = request.getCity();
            String author = request.getLogin();

            if (city == null) {
                return new CommandResponse(false, "Ошибка: объект City не передан");
            }

            if (city.getName() == null || city.getName().trim().isEmpty()) {
                return new CommandResponse(false, "Ошибка: название города не может быть пустым");
            }

            // Добавляем через CollectionManager (сначала в БД, потом в кэш)
            City addedCity = collectionManager.addCity(city, author);

            if (addedCity != null) {
                logger.info("Пользователь '" + author + "' добавил элемент с ID " + addedCity.getId());
                return new CommandResponse(true,
                        "Элемент успешно добавлен в коллекцию с ID: " + addedCity.getId());
            } else {
                return new CommandResponse(false, "Ошибка: не удалось добавить элемент в базу данных");
            }

        } catch (Exception e) {
            logger.severe("Ошибка при добавлении элемента: " + e.getMessage());
            e.printStackTrace();
            return new CommandResponse(false, "Произошла внутренняя ошибка сервера");
        }
    }

    @Override
    public String getName() {
        return "add";
    }

    @Override
    public String getDescription() {
        return "Добавить новый элемент в коллекцию";
    }
}
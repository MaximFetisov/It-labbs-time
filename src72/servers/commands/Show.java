package servers.commands;

import common.CommandRequest;
import common.CommandResponse;
import common.City;
import servers.interfacess.Command;
import servers.managers.CollectionManager;
import servers.managers.DatabaseManager;

import java.util.ArrayList;
import java.util.logging.Logger;

public class Show implements Command {
    private static final Logger logger = Logger.getLogger(Show.class.getName());

    @Override
    public CommandResponse execute(CommandRequest request,
                                   CollectionManager collectionManager,
                                   DatabaseManager databaseManager) {
        try {
            if (request.hasArguments()) {
                return new CommandResponse(false, "У этой команды отсутствуют параметры");
            }

            ArrayList<City> allCities = collectionManager.getCities();

            if (allCities.isEmpty()) {
                return new CommandResponse(true, "Коллекция пуста");
            }

            logger.info("Показано " + allCities.size() + " элементов из кэша");

            return new CommandResponse(true,
                    "Коллекция получена (" + allCities.size() + " элементов)",
                    allCities);

        } catch (Exception e) {
            logger.severe("Ошибка при выводе коллекции: " + e.getMessage());
            e.printStackTrace();
            return new CommandResponse(false,
                    "Ошибка при выводе коллекции: " + e.getMessage());
        }
    }

    @Override
    public String getName() {
        return "show";
    }

    @Override
    public String getDescription() {
        return "Вывести все элементы коллекции";
    }
}
package servers.commands;

import common.CommandRequest;
import common.CommandResponse;
import common.City;
import servers.interfacess.Command;
import servers.managers.CollectionManager;
import servers.managers.FileManager;

import java.util.ArrayList;
import java.util.stream.Collectors;

/**
 * Команда для отображения всех элементов коллекции.
 * Выводит каждый элемент коллекции в строковом представлении.
 * Использует Stream API для сортировки и фильтрации.
 *
 * @author Максим
 * @see Command
 * @see City
 * @see CollectionManager
 */
public class Show implements Command {

    /**
     * Выполняет команду вывода всех элементов коллекции.
     *
     * @param request запрос от клиента
     * @param collectionManager менеджер коллекции
     * @param fileManager менеджер файлов
     * @param dataFile путь к файлу данных
     * @return результат выполнения команды
     */
    @Override
    public CommandResponse execute(CommandRequest request,
                                   CollectionManager collectionManager,
                                   FileManager fileManager,
                                   String dataFile) {
        try {
            if (request.hasArguments()) {
                return new CommandResponse(false,
                        "У этой команды отсутствуют параметры");
            }

            ArrayList<City> cities = collectionManager.getCities();

            if (cities.isEmpty()) {
                return new CommandResponse(true, "Коллекция пуста");
            }

            return new CommandResponse(true,
                    "Коллекция получена (" + cities.size() + " элементов)",
                    cities);

        } catch (Exception e) {
            return new CommandResponse(false,
                    "Ошибка при выводе коллекции: " + e.getMessage());
        }
    }

    @Override
    public String getDescription() {
        return "Вывести все элементы коллекции в строковом представлении";
    }

    @Override
    public String getName() {
        return "show";
    }
}
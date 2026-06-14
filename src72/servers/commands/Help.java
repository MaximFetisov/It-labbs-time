package servers.commands;

import common.CommandRequest;
import common.CommandResponse;
import common.CommandType;
import servers.interfacess.Command;
import servers.managers.CollectionManager;
import servers.managers.DatabaseManager;

/**
 * Команда для вывода справки по всем доступным командам сервера.
 */
public class Help implements Command {

    @Override
    public CommandResponse execute(CommandRequest request,
                                   CollectionManager collectionManager,
                                   DatabaseManager databaseManager) {
        try {
            StringBuilder helpText = new StringBuilder();
            helpText.append("=== Справка по доступным командам (Сервер) ===\n");
            helpText.append("help : Вывести эту справку\n");
            helpText.append("info : Вывести информацию о коллекции\n");
            helpText.append("show : Вывести все элементы коллекции\n");
            helpText.append("clear : Очистить коллекцию (ваши элементы)\n");
            helpText.append("add : Добавить новый элемент\n");
            helpText.append("update : Обновить элемент по ID\n");
            helpText.append("remove_by_id : Удалить элемент по ID\n");
            helpText.append("remove_first : Удалить первый элемент\n");
            helpText.append("remove_lower : Удалить элементы меньшие заданного\n");
            helpText.append("history : Показать историю команд\n");
            helpText.append("sum_of_meters_above_sea_level : Сумма метров над уровнем моря\n");
            helpText.append("min_by_coordinates : Элемент с минимальными координатами\n");
            helpText.append("print_unique_governor : Уникальные губернаторы\n");
            helpText.append("===============================================");

            return new CommandResponse(true, helpText.toString());

        } catch (Exception e) {
            return new CommandResponse(false, "Ошибка при выводе справки: " + e.getMessage());
        }
    }

    @Override
    public String getDescription() {
        return "Вывести справку по доступным командам";
    }

    @Override
    public String getName() {
        return "help";
    }
}
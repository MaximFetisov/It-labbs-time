package servers.commands;

import common.CommandRequest;
import common.CommandResponse;
import servers.interfacess.Command;
import servers.managers.CollectionManager;
import servers.managers.DatabaseManager;

/**
 * Команда для вывода суммы значений поля metersAboveSeaLevel для всех элементов коллекции.
 * Использует Stream API для подсчёта суммы (требование Lab 6).
 * <p>
 * Если поле null, оно пропускается при подсчёте.
 * Чтение доступно всем пользователям (не фильтруется по автору).
 * </p>
 *
 * @author Максим
 * @see Command
 * @see CollectionManager
 */
public class SumOfMeters implements Command {

    @Override
    public CommandResponse execute(CommandRequest request,
                                   CollectionManager collectionManager,
                                   DatabaseManager databaseManager) {
        try {
            if (request.hasArguments()) {
                return new CommandResponse(false,
                        "У этой команды отсутствуют параметры");
            }

            // Проверка на пустую коллекцию
            if (collectionManager.getSize() == 0) {
                return new CommandResponse(true,
                        "Коллекция пуста. Сумма равна 0.");
            }

            // Считаем сумму по ВСЕМ элементам коллекции
            double sum = collectionManager.getCities().stream()
                    .mapToDouble(city ->
                            city.getMetersAboveSeaLevel() != null ?
                                    city.getMetersAboveSeaLevel() : 0.0)
                    .sum();

            long nullCount = collectionManager.getCities().stream()
                    .filter(city -> city.getMetersAboveSeaLevel() == null)
                    .count();

            StringBuilder message = new StringBuilder();
            message.append("=== Сумма метров над уровнем моря ===\n");
            message.append("Суммарное значение: ").append(String.format("%.2f", sum)).append("\n");
            message.append("Элементов с null значением: ").append(nullCount).append("\n");
            message.append("=====================================");

            return new CommandResponse(true, message.toString());

        } catch (Exception e) {
            return new CommandResponse(false,
                    "Ошибка при подсчёте суммы: " + e.getMessage());
        }
    }

    @Override
    public String getDescription() {
        return "Вывести сумму значений поля metersAboveSeaLevel для всех элементов коллекции";
    }

    @Override
    public String getName() {
        return "sum_of_meters_above_sea_level";
    }
}
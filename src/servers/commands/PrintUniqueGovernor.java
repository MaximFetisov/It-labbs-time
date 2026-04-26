package servers.commands;

import common.CommandRequest;
import common.CommandResponse;
import common.Human;
import servers.interfacess.Command;
import servers.managers.CollectionManager;
import servers.managers.FileManager;

import java.util.ArrayList;
import java.util.stream.Collectors;

/**
 * Команда для вывода уникальных значений поля governor всех элементов коллекции.
 * Собирает всех губернаторов через Stream API (с удалением дубликатов через distinct())
 * и выводит их.
 * <p>
 * Использует Stream API для фильтрации и уникализации (требование Lab 6).
 * </p>
 *
 * @author Максим
 * @see Command
 * @see Human
 * @see CollectionManager
 */
public class PrintUniqueGovernor implements Command {

    /**
     * Выполняет команду вывода уникальных губернаторов.
     *
     * @param request запрос от клиента
     * @param collectionManager менеджер коллекции
     * @param fileManager менеджер файлов (не используется для этой команды)
     * @param dataFile путь к файлу данных (не используется для этой команды)
     * @return результат выполнения команды
     */
    @Override
    public CommandResponse execute(CommandRequest request,
                                   CollectionManager collectionManager,
                                   FileManager fileManager,
                                   String dataFile) {
        try {
            // Проверка на аргументы (не должны быть)
            if (request.hasArguments()) {
                return new CommandResponse(false,
                        "У этой команды отсутствуют параметры");
            }

            // Проверка на пустую коллекцию
            if (collectionManager.getSize() == 0) {
                return new CommandResponse(true,
                        "Коллекция пуста. Невозможно вывести губернаторов.");
            }

            // Stream API: получение уникальных губернаторов
            ArrayList<Human> uniqueGovernors = collectionManager.getCities().stream()
                    .map(city -> city.getGovernor())
                    .filter(governor -> governor != null)  // Исключаем null
                    .distinct()  // Удаляем дубликаты (использует Human.compareTo())
                    .collect(Collectors.toCollection(ArrayList::new));

            // Stream API: подсчёт городов без губернатора
            long nullCount = collectionManager.getCities().stream()
                    .filter(city -> city.getGovernor() == null)
                    .count();

            if (uniqueGovernors.isEmpty()) {
                return new CommandResponse(true,
                        "В коллекции нет губернаторов (все значения null). " +
                                "Городов без губернатора: " + nullCount);
            }

            // Формируем сообщение с информацией
            StringBuilder message = new StringBuilder();
            message.append("=== Уникальные губернаторы ===\n");
            message.append("Количество уникальных губернаторов: ")
                    .append(uniqueGovernors.size()).append("\n");
            message.append("Городов без губернатора: ").append(nullCount).append("\n");
            message.append("--------------------------------\n");

            for (Human governor : uniqueGovernors) {
                message.append(governor.toString()).append("\n");
            }

            message.append("==============================");

            return new CommandResponse(true, message.toString());

        } catch (Exception e) {
            return new CommandResponse(false,
                    "Ошибка при выводе уникальных губернаторов: " + e.getMessage());
        }
    }

    @Override
    public String getDescription() {
        return "Вывести уникальные значения поля governor всех элементов в коллекции";
    }

    @Override
    public String getName() {
        return "print_unique_governor";
    }
}
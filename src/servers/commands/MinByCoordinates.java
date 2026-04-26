package servers.commands;

import common.CommandRequest;
import common.CommandResponse;
import common.City;
import servers.interfacess.Command;
import servers.managers.CollectionManager;
import servers.managers.FileManager;

import java.util.Comparator;

/**
 * Команда для вывода объекта из коллекции с минимальными координатами.
 * Сравнивает элементы по полям coordinates.x и coordinates.y с помощью
 * метода {@link common.Coordinates#compareTo(Coordinates)}.
 * Использует Stream API для поиска минимального элемента (требование Lab 6).
 * <p>
 * Если коллекция пуста, выводит соответствующее сообщение.
 * </p>
 *
 * @author Максим
 * @see Command
 * @see City
 * @see CollectionManager
 */
public class MinByCoordinates implements Command {

    /**
     * Выполняет команду поиска элемента с минимальными координатами.
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
            if (request.hasArguments()) {
                return new CommandResponse(false,
                        "У этой команды отсутствуют параметры");
            }

            if (collectionManager.getSize() == 0) {
                return new CommandResponse(true,
                        "Коллекция пуста. Невозможно определить минимальный элемент.");
            }

            City minCity = collectionManager.getCities().stream()
                    .min(Comparator.comparing(City::getCoordinates))
                    .orElse(null);

            if (minCity == null) {
                return new CommandResponse(false,
                        "Не удалось найти элемент с минимальными координатами.");
            }

            StringBuilder message = new StringBuilder();
            message.append("=== Элемент с минимальными координатами ===\n");
            message.append("ID: ").append(minCity.getId()).append("\n");
            message.append("Название: ").append(minCity.getName()).append("\n");
            message.append("Координаты: X=").append(minCity.getCoordinates().getX())
                    .append(", Y=").append(minCity.getCoordinates().getY()).append("\n");
            message.append("Полная информация:\n");
            message.append(minCity.toString()).append("\n");
            message.append("==========================================");

            return new CommandResponse(true, message.toString());

        } catch (Exception e) {
            return new CommandResponse(false,
                    "Ошибка при поиске элемента с минимальными координатами: " + e.getMessage());
        }
    }

    @Override
    public String getDescription() {
        return "Вывести любой объект из коллекции, значение поля coordinates которого является минимальным";
    }

    @Override
    public String getName() {
        return "min_by_coordinates";
    }
}
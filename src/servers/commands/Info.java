package servers.commands;

import common.CommandRequest;
import common.CommandResponse;
import servers.interfacess.Command;
import servers.managers.CollectionManager;
import servers.managers.FileManager;

/**
 * Команда для вывода информации о коллекции.
 * Отображает тип коллекции, количество элементов, дату инициализации
 * и другие мета-данные.
 * <p>
 * Использует Stream API для получения статистики.
 * </p>
 *
 * @author Максим
 * @see Command
 * @see CollectionManager
 */
public class Info implements Command {

    /**
     * Выполняет команду вывода информации о коллекции.
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

            long totalCount = collectionManager.getSize();
            long nullGovernorCount = collectionManager.getCities().stream()
                    .filter(city -> city.getGovernor() == null)
                    .count();
            long nullMetersCount = collectionManager.getCities().stream()
                    .filter(city -> city.getMetersAboveSeaLevel() == null)
                    .count();

            StringBuilder infoText = new StringBuilder();
            infoText.append("=== Информация о коллекции ===\n");
            infoText.append("Тип коллекции: ArrayList<City>\n");
            infoText.append("Количество элементов: ").append(totalCount).append("\n");
            infoText.append("Дата инициализации коллекции: ").append(
                    collectionManager.getInitializationDate()).append("\n");
            infoText.append("Имя файла для работы: ").append(dataFile).append("\n");
            infoText.append("Городов без губернатора: ").append(nullGovernorCount).append("\n");
            infoText.append("Городов с null высотой: ").append(nullMetersCount).append("\n");
            infoText.append("===============================");

            return new CommandResponse(true, infoText.toString());

        } catch (Exception e) {
            return new CommandResponse(false,
                    "Ошибка при выводе информации о коллекции: " + e.getMessage());
        }
    }

    @Override
    public String getDescription() {
        return "Вывести информацию о коллекции (тип, дата инициализации, количество элементов)";
    }

    @Override
    public String getName() {
        return "info";
    }
}
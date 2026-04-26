package servers.commands;

import common.CommandRequest;
import common.CommandResponse;
import servers.interfacess.Command;
import servers.managers.CollectionManager;
import servers.managers.FileManager;

import java.util.Deque;

/**
 * Команда для вывода истории последних 9 выполненных команд.
 * Выводит имена команд без их аргументов в порядке выполнения.
 * <p>
 * История хранится на сервере и передаётся клиенту по запросу.
 * </p>
 *
 * @author Максим
 * @see Command
 * @see CollectionManager
 */
public class History implements Command {

    /**
     * Выполняет команду вывода истории последних команд.
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

            Deque<String> history = collectionManager.getCommandHistory();

            if (history == null || history.isEmpty()) {
                return new CommandResponse(true,
                        "История команд пуста. Выполните несколько команд.");
            }

            StringBuilder historyText = new StringBuilder();
            historyText.append("=== История последних команд (макс. 9) ===\n");

            int commandNumber = 1;
            for (String command : history) {
                historyText.append(commandNumber++).append(". ").append(command).append("\n");
            }

            historyText.append("==========================================");
            historyText.append("\nВсего команд в истории: ").append(history.size());

            return new CommandResponse(true, historyText.toString());

        } catch (Exception e) {
            return new CommandResponse(false,
                    "Ошибка при выводе истории команд: " + e.getMessage());
        }
    }

    @Override
    public String getDescription() {
        return "Вывести последние 9 выполненных команд (без аргументов)";
    }

    @Override
    public String getName() {
        return "history";
    }
}
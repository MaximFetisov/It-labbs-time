package servers.commands;

import common.CommandRequest;
import common.CommandResponse;
import servers.interfacess.Command;
import servers.managers.CollectionManager;
import servers.managers.DatabaseManager;

import java.util.Deque;

/**
 * Команда для вывода истории последних 9 выполненных команд.
 * Выводит имена команд без их аргументов в порядке выполнения.
 */
public class History implements Command {

    @Override
    public CommandResponse execute(CommandRequest request,
                                   CollectionManager collectionManager,
                                   DatabaseManager databaseManager) {
        try {
            if (request.hasArguments()) {
                return new CommandResponse(false, "У этой команды отсутствуют параметры");
            }

            Deque<String> history = collectionManager.getCommandHistory();

            if (history == null || history.isEmpty()) {
                return new CommandResponse(true, "История команд пуста. Выполните несколько команд.");
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
            return new CommandResponse(false, "Ошибка при выводе истории команд: " + e.getMessage());
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
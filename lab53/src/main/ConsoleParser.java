package main;

import commands.*;
import interfaces.Command;
import java.util.HashMap;
import java.util.Map;

/**
 * Менеджер для парсинга и выполнения пользовательских команд.
 * Содержит словарь доступных команд и обеспечивает их выполнение.
 * Все команды регистрируются в конструкторе и выполняются через интерфейс {@link Command}.
 *

 * @see Command
 * @see Run
 */
public class ConsoleParser {
    private Map<String, Command> commands;

    public ConsoleParser() {
        commands = new HashMap<>();
        registerCommands();
    }

    /**
     * Регистрирует все доступные команды в словаре.
     * Каждая команда связывается со своим именем для последующего вызова.
     */
    private void registerCommands() {
        commands.put("help", new Help());
        commands.put("info", new Info());
        commands.put("show", new Show());
        commands.put("clear", new Clear());
        commands.put("exit", new Exit());

        commands.put("add", new Add());
        commands.put("update", new Update());
        commands.put("remove_by_id", new RemoveById());
        commands.put("remove_first", new RemoveFirst());
        commands.put("remove_lower", new RemoveLower());
        commands.put("save", new Save());

        commands.put("history", new History());
        commands.put("sum_of_meters_above_sea_level", new SumOfMeters());
        commands.put("min_by_coordinates", new MinByCoordinates());
        commands.put("print_unique_governor", new PrintUniqueGovernor());

        commands.put("execute_script", new Execute_script());
    }

    /**
     * Выполняет парсинг и обработку введённой пользователем команды.
     * Ищет команду в словаре и вызывает её метод {@link Command#execute(String[])}.
     * Если команда не найдена или при её выполнении возникла ошибка, возвращает false.
     *
     * @param commandName имя команды
     * @param args аргументы команды (строка с аргументами)
     * @return true, если команда успешно найдена и выполнена, иначе false
     * @see Run#inout
     * @see Command#execute(String[])
     */
    public boolean parse(String commandName, String args) {
        if (commands.containsKey(commandName)) {
            Command command = commands.get(commandName);
            try {
                String[] arguments = args.trim().isEmpty() ? new String[0] : args.trim().split("\\s+");
                boolean success = command.execute(arguments);
                return success;
            } catch (Exception e) {
                Run.inout.write("Ошибка при выполнении команды: " + e.getMessage());
                Run.inout.setScriptError(true);
                return false;
            }
        } else {
            return false;
        }
    }

    /**
     * Возвращает словарь всех зарегистрированных команд.
     *
     * @return Map с именами команд и их объектами
     */
    public Map<String, Command> getCommands() {
        return commands;
    }

    /**
     * Проверяет, существует ли команда с указанным именем.
     *
     * @param commandName имя команды для проверки
     * @return true, если команда существует, иначе false
     */
    public boolean hasCommand(String commandName) {
        return commands.containsKey(commandName);
    }
}
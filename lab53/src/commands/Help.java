package commands;

import interfaces.Command;
import main.Run;
import java.util.Map;

/**
 * Команда для вывода справки по всем доступным командам.
 * Отображает список всех зарегистрированных команд с их описаниями.
 *
 * @author Максим
 * @see Command
 * @see Run#consoleParser
 */
public class Help implements Command {

    /**
     * Выполняет команду вывода справки по всем доступным командам.
     *
     * @param args аргументы команды (не используются)
     * @return true при успешном выполнении, false при ошибке
     */
    @Override
    public boolean execute(String[] args) {
        if (args.length>0){
            Run.inout.write("У этой команды отсутствуют параметры. Введите команду без них");
            return true;
        }
        try {
            Run.inout.write("=== Справка по доступным командам ===");
            Map<String, Command> commands = Run.consoleParser.getCommands();
            for (Command cmd : commands.values()) {
                Run.inout.write(cmd.getName() + " : " + cmd.getDescription());
            }
            Run.inout.write("=====================================");
            return true;
        } catch (Exception e) {
            Run.inout.write("Ошибка при выводе справки: " + e.getMessage());
            Run.inout.setScriptError(true);
            return false;
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
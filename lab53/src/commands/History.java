package commands;

import interfaces.Command;
import main.Run;

/**
 * Команда для вывода истории последних 9 выполненных команд.
 * Выводит имена команд без их аргументов в порядке выполнения.
 *
 * @author Максим
 * @see Command
 * @see Run#commandHistory
 */
public class History implements Command {

    /**
     * Выполняет команду вывода истории последних команд.
     *
     * @param args аргументы команды (не используются)
     * @return true при успешном выполнении, false при ошибке
     */
    @Override
    public boolean execute(String[] args) {
        if (args.length>0){
            Run.inout.write("У этой команды отсутствуют параметры");
            return true;
        }
        for (String command : Run.commandHistory) {
            Run.inout.write(command);
        }
        return true;
    }

    @Override
    public String getDescription() {
        return "вывести последние 9 команд";
    }

    @Override
    public String getName() {
        return "history";
    }
}
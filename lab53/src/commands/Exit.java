package commands;

import interfaces.Command;
import main.Run;

/**
 * Команда для завершения работы программы.
 * Корректно закрывает все ресурсы ввода-вывода и завершает выполнение JVM.
 * Не сохраняет коллекцию в файл (для сохранения используйте команду save).

 * @see Command
 * @see Run#inout
 */
public class Exit implements Command {
    @Override
    public boolean execute(String[] args) {
        if (args.length>0){
            Run.inout.write("У этой команды нет параметров");
            return true;
        }
        try {
            Run.inout.write("Завершение работы программы...");
            Run.inout.closeScan();
            System.exit(0);
            return true;
        } catch (Exception e) {
            Run.inout.write("Ошибка при завершении работы: " + e.getMessage());
            Run.inout.setScriptError(true);
            return false;
        }
    }

    @Override
    public String getDescription() {
        return "Завершить программу (без сохранения в файл)";
    }

    @Override
    public String getName() {
        return "exit";
    }
}
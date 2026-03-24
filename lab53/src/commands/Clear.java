package commands;

import interfaces.Command;
import main.Run;

/**
 * Команда для очистки коллекции.
 * Удаляет все элементы из {@link Run#collectionManager}.
 *
 * @author Максим
 * @see Command
 * @see Run#collectionManager
 */
public class Clear implements Command {
    /**
     * Выполняет команду очистки коллекции.
     *
     * @param args аргументы команды (не должны быть указаны)
     * @return true при успешном выполнении, false при ошибке
     */
    @Override
    public boolean execute(String[] args) {
        if (args.length>0){
            Run.inout.write("У этой команды отсутствуют параметры");
            return true;
        }
        try {
            Run.collectionManager.clear();
            Run.inout.write("Вся коллекция была удалена");
            return true;

        } catch (Exception e) {
            Run.inout.write("Ошибка при очистке коллекции: " + e.getMessage());
            Run.inout.setScriptError(true);
            return false;
        }
    }

    @Override
    public String getDescription() {
        return "Очищает коллекцию";
    }

    @Override
    public String getName() {
        return "clear";
    }
}
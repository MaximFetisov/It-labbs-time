package commands;

import interfaces.Command;
import main.Run;

/**
 * Команда для удаления первого элемента из коллекции.
 * Удаляет элемент с индексом 0 из {@link ArrayList} коллекции {@link Run#collectionManager}.
 * Если коллекция пуста, выводит соответствующее сообщение.
 *
 * @author Максим
 * @see Command
 * @see Run#collectionManager
 */
public class RemoveFirst implements Command {
    @Override
    public boolean execute(String[] args) {
        if (args.length>0){
            Run.inout.write("У этой команды отсутствуют параметры");
            return true;
        }
        try {
            if (Run.collectionManager.getCities().isEmpty()) {
                Run.inout.write("Коллекция пуста. Нечего удалять.");
                return true;
            }
            
            Run.collectionManager.getCities().remove(0);
            Run.inout.write("Первый элемент успешно удалён из коллекции.");
            return true;
        } catch (Exception e) {
            Run.inout.write("Ошибка при удалении первого элемента: " + e.getMessage());
            Run.inout.setScriptError(true);
            return false;
        }
    }

    @Override
    public String getDescription() {
        return "Удалить первый элемент из коллекции";
    }

    @Override
    public String getName() {
        return "remove_first";
    }
}
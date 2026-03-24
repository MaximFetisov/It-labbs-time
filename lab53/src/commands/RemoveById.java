package commands;

import datass.City;
import interfaces.Command;
import main.ChackValues;
import main.Run;

/**
 * Команда для удаления элемента из коллекции по его идентификатору.
 * Находит элемент с указанным {@code id} и удаляет его из коллекции.
 * Если элемент не найден, выводит соответствующее сообщение.
 *
 * @author Максим
 * @see Command
 * @see City
 * @see Run#collectionManager
 */
public class RemoveById implements Command {
    String idStr;
    @Override
    public boolean execute(String[] args) {
        if (args.length==0){
            Run.inout.write("ID объекта, который надо обновить: ");
            idStr = ChackValues.chackValuesNull("id элемента");
        } else {
            idStr=args[0];
        }
        try {
            long id = Long.parseLong(idStr);
            
            if (Run.collectionManager.removeById(id)) {
                Run.inout.write("Элемент с id " + id + " успешно удалён.");
                return true;
            } else {
                Run.inout.write("Элемент с id " + id + " не найден в коллекции.");
                return true;
            }
        } catch (NumberFormatException e) {
            Run.inout.write("Ошибка: id должен быть числом типа long.");
            Run.inout.setScriptError(true);
            return false;
        } catch (Exception e) {
            Run.inout.write("Ошибка при удалении элемента: " + e.getMessage());
            Run.inout.setScriptError(true);
            return false;
        }
    }

    @Override
    public String getDescription() {
        return "Удалить элемент из коллекции по его id";
    }

    @Override
    public String getName() {
        return "remove_by_id";
    }
}
package commands;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import datass.City;
import interfaces.Command;
import main.ChackValues;
import main.Run;

/**
 * Команда для обновления значения элемента коллекции по его идентификатору.
 * Заменяет существующий элемент новым объектом {@link City} с сохранением указанного id.
 *
 * @author Максим
 * @see Command
 * @see City
 * @see Run#collectionManager
 */
public class Update implements Command {
    String idlong;
    Map idMap = new HashMap();

    /**
     * Выполняет команду обновления элемента коллекции по id.
     *
     * @param args аргументы команды (содержат id элемента для обновления)
     * @return true при успешном выполнении, false при ошибке
     */
   @Override
public boolean execute(String[] args) {
    idMap.clear();
    for (int i = 0; i < Run.collectionManager.getCities().size(); i++) {
        idMap.put(Run.collectionManager.getCities().get(i).getId(), i);
    }
    
    if (args.length == 0) {
        Run.inout.write("ID объекта, который надо обновить: ");
        idlong = ChackValues.chackValuesNull("Ключ объекта для обновления: ");
    } else if (args.length == 1) {
        idlong = args[0].trim();
    } else {
        Run.inout.write("Больше одного параметра, считывается только первый");
        idlong = args[0].trim();
    }
    
    try {
        long id = Long.parseLong(idlong);
        if (idMap.containsKey(id)) {
            Run.collectionManager.getCities().set((int) idMap.get(id), new City(id));
            Run.inout.write(Run.collectionManager.getCities().get((int) idMap.get(id)).toString());
        } else {
            Run.inout.write("Элемента с таким id нет в коллекции.");
        }
    } catch (NumberFormatException e) {
        Run.inout.write("Значение должно быть long.");
    }
    return true;
}

    @Override
    public String getDescription() {
        return "Обновить значение элемента коллекции, id которого равен заданному";
    }

    @Override
    public String getName() {
        return "update";
    }
}
package commands;

import datass.City;
import interfaces.Command;
import main.Run;
import java.util.Iterator;

/**
 * Команда для удаления всех элементов, которые меньше заданного.
 * Создаёт новый объект {@link City} через пользовательский ввод,
 * затем удаляет все элементы коллекции, которые меньше этого эталона
 * (согласно методу {@link City#compareTo(City)}).
 *
 * @author Максим
 * @see Command
 * @see City
 * @see Run#collectionManager
 */
public class RemoveLower implements Command {
    @Override
    public boolean execute(String[] args) {
        if (args.length>0){
            Run.inout.write("У этой команды отсутствуют параметры");
            return true;
        }
        try {
            Run.inout.write("Введите элемент для сравнения (будет создан новый объект City):");
            City referenceCity = Run.cityForm.build();
            
            int removedCount = 0;
            Iterator<City> iterator = Run.collectionManager.getCities().iterator();
            
            while (iterator.hasNext()) {
                City city = iterator.next();
                if (city.compareTo(referenceCity) < 0) {
                    iterator.remove();
                    removedCount++;
                }
            }
            
            Run.inout.write("Удалено элементов, меньших заданного: " + removedCount);
            return true;
        } catch (Exception e) {
            Run.inout.write("Ошибка при выполнении команды: " + e.getMessage());
            Run.inout.setScriptError(true);
            return false;
        }
    }

    @Override
    public String getDescription() {
        return "Удалить из коллекции все элементы, меньшие, чем заданный";
    }

    @Override
    public String getName() {
        return "remove_lower";
    }
}
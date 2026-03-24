package commands;

import datass.City;
import interfaces.Command;
import main.Run;

/**
 * Команда для отображения всех элементов коллекции.
 * Выводит каждый элемент коллекции в строковом представлении.
 * Если коллекция пуста, выводит соответствующее сообщение.
 *

 * @see Command
 * @see City
 * @see Run#collectionManager
 */
public class Show implements Command {
    @Override
    public boolean execute(String[] args) {
        if (args.length>0){
            Run.inout.write("У этой команды отсутствуют параметры");
            return true;
        }
        try {
            if (Run.collectionManager.getCities().isEmpty()) {
                Run.inout.write("Коллекция пуста");
                return true;
            }
            
            Run.inout.write("=== Все элементы коллекции ===");
            Run.inout.write("ID | Название | Координаты | Дата создания | Площадь | Население | Высота | Климат | Правительство | Уровень жизни | Губернатор");
            
            for (City city : Run.collectionManager.getCities()) {
                Run.inout.write(city.toString());
            }
            
            Run.inout.write("================================");
            return true;
        } catch (Exception e) {
            Run.inout.write("Ошибка при выводе коллекции: " + e.getMessage());
            Run.inout.setScriptError(true);
            return false;
        }
    }

    @Override
    public String getDescription() {
        return "Вывести все элементы коллекции в строковом представлении";
    }

    @Override
    public String getName() {
        return "show";
    }
}
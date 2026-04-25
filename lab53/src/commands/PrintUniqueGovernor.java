package commands;

import datass.City;
import datass.Human;
import interfaces.Command;
import main.Run;
import java.util.TreeSet;

/**
 * Команда для вывода уникальных значений поля governor всех элементов коллекции.
 * Собирает всех губернаторов в TreeSet (что автоматически удаляет дубликаты
 * благодаря естественному порядку сортировки {@link Human#compareTo(Human)})
 * и выводит их.
 *
 * @author Максим
 * @see Command
 * @see Human
 * @see Run#collectionManager
 */
public class PrintUniqueGovernor implements Command {
    @Override
    public boolean execute(String[] args) {
        if (args.length>0){
            Run.inout.write("У этой команды отсутствуют параметры");
            return true;
        }
        try {
            if (Run.collectionManager.getCities().isEmpty()) {
                Run.inout.write("Коллекция пуста.");
                return true;
            }

            TreeSet<Human> governors = new TreeSet<>();
            int nullCount = 0;

            for (City city : Run.collectionManager.getCities()) {
                Human governor = city.getGovernor();
                if (governor != null) {
                    governors.add(governor);
                } else {
                    nullCount++;
                }
            }

            if (governors.isEmpty()) {
                Run.inout.write("В коллекции нет губернаторов (все значения null).");
                return true;
            }

            Run.inout.write("=== Уникальные губернаторы ===");
            Run.inout.write("Количество уникальных губернаторов: " + governors.size());
            Run.inout.write("Городов без губернатора: " + nullCount);
            Run.inout.write("--------------------------------");
            
            for (Human governor : governors) {
                Run.inout.write(governor.toString());
            }
            
            Run.inout.write("==============================");
            return true;
        } catch (Exception e) {
            Run.inout.write("Ошибка при выводе уникальных губернаторов: " + e.getMessage());
            Run.inout.setScriptError(true);
            return false;
        }
    }

    @Override
    public String getDescription() {
        return "Вывести уникальные значения поля governor всех элементов в коллекции";
    }

    @Override
    public String getName() {
        return "print_unique_governor";
    }
}
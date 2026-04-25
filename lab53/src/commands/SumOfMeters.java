package commands;

import datass.City;
import interfaces.Command;
import main.Run;

/**
 * Команда для вывода суммы значений поля metersAboveSeaLevel для всех элементов коллекции.
 * Проходит по всем элементам коллекции и суммирует значения высоты над уровнем моря.
 * Если поле null, оно пропускается при подсчёте.
 *
 * @author Максим
 * @see Command
 * @see City
 * @see Run#collectionManager
 */
public class SumOfMeters implements Command {
    @Override
    public boolean execute(String[] args) {
        if (args.length>0){
            Run.inout.write("У этой команды отсутствуют параметры");
            return true;
        }
        try {
            if (Run.collectionManager.getCities().isEmpty()) {
                Run.inout.write("Коллекция пуста. Сумма равна 0.");
                return true;
            }

            float sum = 0.0f;
            int nullCount = 0;

            for (City city : Run.collectionManager.getCities()) {
                Float meters = city.getMetersAboveSeaLevel();
                if (meters != null) {
                    sum += meters;
                } else {
                    nullCount++;
                }
            }

            Run.inout.write("=== Сумма метров над уровнем моря ===");
            Run.inout.write("Суммарное значение: " + sum);
            Run.inout.write("Элементов с null значением: " + nullCount);
            Run.inout.write("=====================================");
            return true;
        } catch (Exception e) {
            Run.inout.write("Ошибка при подсчёте суммы: " + e.getMessage());
            Run.inout.setScriptError(true);
            return false;
        }
    }

    @Override
    public String getDescription() {
        return "Вывести сумму значений поля metersAboveSeaLevel для всех элементов коллекции";
    }

    @Override
    public String getName() {
        return "sum_of_meters_above_sea_level";
    }
}
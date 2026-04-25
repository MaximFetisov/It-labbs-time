package commands;

import interfaces.Command;
import main.Run;

/**
 * Команда для вывода информации о коллекции.
 * Отображает тип коллекции, количество элементов, дату инициализации
 * и другие мета-данные о коллекции {@link Run#collectionManager}.
 *

 * @see Command
 * @see Run#collectionManager
 */
public class Info implements Command {
    @Override
    public boolean execute(String[] args) {
        if (args.length>0){
            Run.inout.write("У этой команды отсутствуют параметры");
            return true;
        }
        try {
            Run.inout.write("=== Информация о коллекции ===");
            Run.inout.write("Тип коллекции: ArrayList<City>");
            Run.inout.write("Количество элементов: " + Run.collectionManager.getSize());
            Run.inout.write("Дата инициализации коллекции: " + Run.collectionManager.getInitializationDate());
            Run.inout.write("Имя файла для работы: " + Run.nameFile);
            Run.inout.write("===============================");
            return true;
        } catch (Exception e) {
            Run.inout.write("Ошибка при выводе информации о коллекции: " + e.getMessage());
            Run.inout.setScriptError(true);
            return false;
        }
    }

    @Override
    public String getDescription() {
        return "Вывести информацию о коллекции (тип, дата инициализации, количество элементов)";
    }

    @Override
    public String getName() {
        return "info";
    }
}
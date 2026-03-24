package commands;

import datass.City;
import interfaces.Command;
import main.ConsoleParser;
import main.Run;

/**
 * Команда для добавления нового элемента в коллекцию.
 * Создаёт новый объект {@link City} и добавляет его в {@link Run#collectionManager}.
 *
 * @author Максим
 * @see Command
 * @see City
 */
public class Add implements Command {

    /**
     * Выполняет команду добавления элемента в коллекцию.
     *
     * @param args аргументы команды (не используются)
     * @return true при успешном выполнении, false при ошибке
     */
    @Override
    public boolean execute(String[] args) {
        if (args.length>0){
            Run.inout.write("У этой команды нет параметров. Значение элемента вводится позже");
            return true;
        }
        try {
            Run.inout.write("Добавление нового элемента в коллекцию:");
            City city = Run.cityForm.build();
            Run.collectionManager.add(city);
            Run.inout.write("Элемент успешно добавлен с ID: " + city.getId());
            return true;
        } catch (Exception e) {
            Run.inout.write("Ошибка при добавлении элемента: " + e.getMessage());
            Run.inout.setScriptError(true);
            return false;
        }
    }

    /**
     * Возвращает описание команды для справки.
     *
     * @return описание команды
     */
    @Override
    public String getDescription() {
        return "Добавляет новый элемент в коллекцию";
    }

    /**
     * Возвращает имя команды для регистрации в {@link ConsoleParser}.
     *
     * @return имя команды
     */
    @Override
    public String getName() {
        return "add";
    }
}
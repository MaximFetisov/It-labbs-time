package commands;

import java.io.IOException;

import interfaces.Command;
import main.FileParser;
import main.Run;

/**
 * Команда для сохранения коллекции в файл.
 * Записывает текущее состояние коллекции {@link Run#collectionManager} в JSON-файл
 * через {@link FileParser#writeToJsonFile(String, java.util.ArrayList)}.
 *
 * @author Максим
 * @see Command
 * @see Run#fileParser
 */
public class Save implements Command {

    /**
     * Выполняет команду сохранения коллекции в файл.
     *
     * @param args аргументы команды (не используются)
     * @return true при успешном выполнении, false при ошибке
     */
    @Override
    public boolean execute(String[] args) {
        if (args.length>0){
            Run.inout.write("У этой команды отсутствуют параметры");
            return true;
        }
        try {
            Run.fileParser.writeToJsonFile(Run.nameFile, Run.collectionManager.getCities());
        } catch (IOException e) {
            Run.inout.write("бебебебебебе");
        }
        return true;
    }

    @Override
    public String getDescription() {
        return "Сохраняет коллекцию в нужный файл";
    }

    @Override
    public String getName() {
        return "save";
    }
}
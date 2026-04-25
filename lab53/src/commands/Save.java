package commands;

import java.io.IOException;

import interfaces.Command;
import main.Run;

/**
 * Команда для сохранения коллекции в файл.
 */
public class Save implements Command {
    
    @Override
    public boolean execute(String[] args) {
        if (args.length > 0) {
            Run.inout.write("У этой команды отсутствуют параметры");
            return false;
        }
        
        try {
            Run.fileParser.writeToJsonFile(Run.nameFile, Run.collectionManager.getCities());
            Run.inout.write("Коллекция успешно сохранена в файл: " + Run.nameFile);
            return true;
            
        } catch (java.nio.file.AccessDeniedException e) {
            Run.inout.write("Ошибка: нет прав на запись в файл '" + Run.nameFile + "'");
            Run.inout.write("Попробуйте запустить программу с правами администратора " +
                          "или выберите другой путь для сохранения");
            return false;
            
        } catch (java.io.FileNotFoundException e) {
            Run.inout.write("Ошибка: путь не найден: " + Run.nameFile);
            return false;
            
        } catch (IOException e) {
            Run.inout.write("Ошибка ввода-вывода: " + e.getMessage());
            return false;
            
        } catch (NullPointerException e) {
            Run.inout.write("Внутренняя ошибка: файл или коллекция не инициализированы");
            return false;
        }
    }

    @Override
    public String getDescription() {
        return "Сохраняет коллекцию в файл";
    }

    @Override
    public String getName() {
        return "save";
    }
}
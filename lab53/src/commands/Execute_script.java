package commands;

import interfaces.Command;
import main.Run;

/**
 * Команда для выполнения скрипта из файла.
 * Читает указанный файл построчно и последовательно выполняет содержащиеся в нём команды.
 * При возникновении ошибки выполнение скрипта прерывается.
 * Поддерживает вложенные вызовы скриптов (с защитой от бесконечной рекурсии).
 *
 * @author Максим
 * @see Command
 * @see Run#inout
 * @see Run#consoleParser
 */
public class Execute_script implements Command {
    // Счётчик вложенности скриптов для защиты от рекурсии
    private static int scriptDepth = 0;
    private static final int MAX_SCRIPT_DEPTH = 10;

    @Override
    public boolean execute(String[] args) {
        try {
            // Проверка на наличие аргумента (имя файла)
            if (args.length == 0 || args[0].trim().isEmpty()) {
                Run.inout.write("Ошибка: не указано имя файла скрипта.");
                Run.inout.write("Использование: execute_script <имя_файла>");
                return false;
            }

            String filename = args[0].trim();

            // Защита от бесконечной рекурсии
            if (scriptDepth >= MAX_SCRIPT_DEPTH) {
                Run.inout.write("Ошибка: превышена максимальная глубина вложенности скриптов (" + MAX_SCRIPT_DEPTH + ")");
                Run.inout.setScriptError(true);
                return false;
            }

            // Переключаем режим ввода на файл
            Run.inout.startFileReading(filename);
            
            // Если файл не открылся, выходим
            if (!Run.inout.isReadingFromFile()) {
                Run.inout.setScriptError(true);
                return false;
            }

            scriptDepth++;
            Run.inout.write("Выполнение скрипта из файла: " + filename);

            // Читаем и выполняем команды из файла
            while (Run.inout.hasNextLine()) {
                String command = Run.inout.read();
                
                // Пропускаем пустые строки
                if (command == null || command.trim().isEmpty()) {
                    continue;
                }

                // Разбираем команду на имя и аргументы
                String[] tokens = command.trim().split("\\s+", 2);
                String commandName = tokens[0];
                String commandArgs = tokens.length > 1 ? tokens[1] : "";

                // Выполняем команду
                boolean success = Run.consoleParser.parse(commandName, commandArgs);

                // Если ошибка — прерываем скрипт
                if (!success || Run.inout.isScriptHasError()) {
                    Run.inout.write("Ошибка в скрипте на команде: " + commandName);
                    scriptDepth--;
                    Run.inout.stopFileReading();
                    Run.inout.setScriptError(true);
                    return false;
                }
            }

            scriptDepth--;
            Run.inout.stopFileReading();
            Run.inout.write("Скрипт успешно выполнен.");
            return true;

        } catch (Exception e) {
            Run.inout.write("Ошибка при выполнении скрипта: " + e.getMessage());
            Run.inout.setScriptError(true);
            scriptDepth = 0; // Сброс при критической ошибке
            Run.inout.stopFileReading();
            return false;
        }
    }

    @Override
    public String getDescription() {
        return "Считать и исполнить скрипт из указанного файла";
    }

    @Override
    public String getName() {
        return "execute_script";
    }
}
package client.commands;

import client.forms.CityForm;
import client.managers.InputManager;
import client.managers.NetworkManager;
import client.managers.ValidationManager;
import common.CommandRequest;
import common.CommandResponse;
import common.CommandType;
import common.City;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.DatagramSocket;

/**
 * Клиентская команда ExecuteScript.
 * Читает указанный файл построчно и последовательно отправляет
 * содержащиеся в нём команды на сервер для выполнения.
 * <p>
 * При возникновении ошибки выполнение скрипта прерывается.
 * Поддерживает вложенные вызовы скриптов с ограничением глубины рекурсии.
 * Поддерживает команды с параметрами из файла (add, update, remove_lower).
 * </p>
 */
public class ExecuteScript {
    // Счётчик вложенности скриптов для защиты от бесконечной рекурсии
    private int scriptDepth = 0;
    private static final int MAX_SCRIPT_DEPTH = 10;

    /**
     * Выполняет команду чтения и исполнения скрипта из файла.
     */
    public CommandResponse execute(DatagramSocket socket,
                                   NetworkManager networkManager,
                                   String host,
                                   int port,
                                   String filename) {
        // Проверка на наличие аргумента (имя файла)
        if (filename == null || filename.trim().isEmpty()) {
            return new CommandResponse(false,
                    "Ошибка: не указано имя файла скрипта.\nИспользование: execute_script <имя_файла>");
        }

        filename = filename.trim();

        // Проверка существования файла
        File scriptFile = new File(filename);
        if (!scriptFile.exists() || !scriptFile.canRead()) {
            return new CommandResponse(false,
                    "Ошибка: файл не найден или не доступен для чтения: " + filename);
        }

        // Проверка глубины рекурсии
        if (scriptDepth >= MAX_SCRIPT_DEPTH) {
            return new CommandResponse(false,
                    "Ошибка: превышена максимальная глубина вложенности скриптов (" + MAX_SCRIPT_DEPTH + ")");
        }

        scriptDepth++;
        System.out.println("Выполнение скрипта из файла: " + filename + " (глубина: " + scriptDepth + ")");

        ValidationManager validationManager = new ValidationManager();
        InputManager inputManager = new InputManager();
        CityForm cityForm = new CityForm();

        try (BufferedReader reader = new BufferedReader(new FileReader(scriptFile))) {
            String line;
            int lineNumber = 0;

            while ((line = reader.readLine()) != null) {
                lineNumber++;
                line = line.trim();

                // Пропускаем пустые строки и комментарии
                if (line.isEmpty() || line.startsWith("#") || line.startsWith("//")) {
                    continue;
                }

                // Разбираем команду на имя и аргументы
                String[] tokens = line.split("\\s+", 2);
                String commandName = tokens[0].toLowerCase();
                String commandArgs = tokens.length > 1 ? tokens[1] : "";

                //  ОБРАБОТКА execute_script — рекурсивный вызов
                if (commandName.equalsIgnoreCase("execute_script")) {
                    if (commandArgs.isEmpty()) {
                        System.err.println("Ошибка в скрипте (строка " + lineNumber + "): не указано имя файла для execute_script");
                        scriptDepth--;
                        return new CommandResponse(false,
                                "Скрипт прерван: не указано имя файла для execute_script на строке " + lineNumber);
                    }

                    String nestedFilename = commandArgs.trim();
                    System.out.println("[" + lineNumber + "] Вложенный вызов: execute_script " + nestedFilename);

                    //  Рекурсивный вызов
                    CommandResponse nestedResponse = execute(socket, networkManager, host, port, nestedFilename);

                    if (!nestedResponse.isSuccess()) {
                        System.err.println("Ошибка в скрипте (строка " + lineNumber + "): ошибка во вложенном скрипте");
                        scriptDepth--;
                        return new CommandResponse(false,
                                "Скрипт прерван: ошибка во вложенном скрипте '" + nestedFilename + "'");
                    }

                    System.out.println("[" + lineNumber + "] Вложенный скрипт завершён: " + nestedFilename);
                    continue;
                }

                // Валидация команды
                CommandType type = CommandType.parseCommand(commandName);
                if (type == CommandType.UNKNOWN) {
                    System.err.println("Ошибка в скрипте (строка " + lineNumber + "): неизвестная команда '" + commandName + "'");
                    scriptDepth--;
                    return new CommandResponse(false,
                            "Скрипт прерван: неизвестная команда '" + commandName + "' на строке " + lineNumber);
                }

                // Проверка доступности команды на клиенте
                if (!validationManager.isCommandAvailable(type)) {
                    System.err.println("Ошибка в скрипте (строка " + lineNumber + "): команда '" + commandName + "' недоступна на клиенте");
                    scriptDepth--;
                    return new CommandResponse(false,
                            "Скрипт прерван: команда '" + commandName + "' недоступна на клиенте");
                }

                // Проверка аргументов
                String[] argsArray = commandArgs.isEmpty() ? new String[0] : commandArgs.split("\\s+");
                if (!validationManager.validateArguments(type, argsArray)) {
                    System.err.println("Ошибка в скрипте (строка " + lineNumber + "): неверные аргументы для команды '" + commandName + "'");
                    scriptDepth--;
                    return new CommandResponse(false,
                            "Скрипт прерван: неверные аргументы для команды '" + commandName + "' на строке " + lineNumber);
                }

                //  Обработка команд, требующих ввода данных (из файла скрипта)
                City city = null;
                if (type == CommandType.ADD || type == CommandType.UPDATE || type == CommandType.REMOVE_LOWER) {
                    System.out.println("[" + lineNumber + "] Чтение параметров для команды '" + commandName + "' из файла...");

                    city = cityForm.buildFromFile(reader, lineNumber);

                    if (city == null) {
                        System.err.println("Ошибка в скрипте (строка " + lineNumber + "): не удалось прочитать параметры для команды '" + commandName + "'");
                        scriptDepth--;
                        return new CommandResponse(false,
                                "Скрипт прерван: ошибка при чтении параметров для команды '" + commandName + "'");
                    }
                }

                // Создание запроса
                CommandRequest request;
                if (city != null) {
                    request = new CommandRequest(type, argsArray, city);
                } else {
                    request = inputManager.createRequest(type, commandArgs);
                }

                if (request == null) {
                    System.err.println("Ошибка в скрипте (строка " + lineNumber + "): не удалось создать запрос для команды '" + commandName + "'");
                    scriptDepth--;
                    return new CommandResponse(false,
                            "Скрипт прерван: ошибка при создании запроса для команды '" + commandName + "'");
                }

                // Отправка запроса на сервер
                CommandResponse response = networkManager.sendRequest(socket, request, host, port);

                if (response == null) {
                    System.err.println("Ошибка в скрипте (строка " + lineNumber + "): сервер не ответил");
                    scriptDepth--;
                    return new CommandResponse(false,
                            "Скрипт прерван: сервер не ответил на команду '" + commandName + "'");
                }

                // Вывод результата
                if (response.isSuccess()) {
                    System.out.println("[" + lineNumber + "] " + commandName + ": " + response.getMessage());
                } else {
                    System.err.println("Ошибка в скрипте (строка " + lineNumber + "): " + response.getMessage());
                    scriptDepth--;
                    return new CommandResponse(false,
                            "Скрипт прерван: ошибка при выполнении '" + commandName + "': " + response.getMessage());
                }
            }

            scriptDepth--;
            System.out.println("Скрипт успешно выполнен: " + filename);
            return new CommandResponse(true, "Скрипт успешно выполнен: " + filename);

        } catch (IOException e) {
            scriptDepth = 0; // Сброс при критической ошибке
            return new CommandResponse(false,
                    "Ошибка при чтении файла скрипта: " + e.getMessage());
        } catch (Exception e) {
            scriptDepth = 0;
            return new CommandResponse(false,
                    "Неожиданная ошибка при выполнении скрипта: " + e.getMessage());
        }
    }
}
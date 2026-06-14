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
 */
public class ExecuteScript {
    private int scriptDepth = 0;
    private static final int MAX_SCRIPT_DEPTH = 10;

    public CommandResponse execute(DatagramSocket socket,
                                   NetworkManager networkManager,
                                   String host,
                                   int port,
                                   String filename,
                                   String login,
                                   String password) {
        if (filename == null || filename.trim().isEmpty()) {
            return new CommandResponse(false,
                    "Ошибка: не указано имя файла скрипта.\nИспользование: execute_script <имя_файла>");
        }

        filename = filename.trim();

        File scriptFile = new File(filename);
        if (!scriptFile.exists() || !scriptFile.canRead()) {
            return new CommandResponse(false,
                    "Ошибка: файл не найден или не доступен для чтения: " + filename);
        }

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

                if (line.isEmpty() || line.startsWith("#") || line.startsWith("//")) {
                    continue;
                }

                String[] tokens = line.split("\\s+", 2);
                String commandName = tokens[0].toLowerCase();
                String commandArgs = tokens.length > 1 ? tokens[1] : "";

                if (commandName.equalsIgnoreCase("execute_script")) {
                    if (commandArgs.isEmpty()) {
                        System.err.println("Ошибка в скрипте (строка " + lineNumber + "): не указано имя файла для execute_script");
                        scriptDepth--;
                        return new CommandResponse(false,
                                "Скрипт прерван: не указано имя файла для execute_script на строке " + lineNumber);
                    }

                    String nestedFilename = commandArgs.trim();
                    System.out.println("[" + lineNumber + "] Вложенный вызов: execute_script " + nestedFilename);

                    CommandResponse nestedResponse = execute(socket, networkManager, host, port, nestedFilename, login, password);

                    if (!nestedResponse.isSuccess()) {
                        System.err.println("Ошибка в скрипте (строка " + lineNumber + "): ошибка во вложенном скрипте");
                        scriptDepth--;
                        return new CommandResponse(false,
                                "Скрипт прерван: ошибка во вложенном скрипте '" + nestedFilename + "'");
                    }

                    System.out.println("[" + lineNumber + "] Вложенный скрипт завершён: " + nestedFilename);
                    continue;
                }

                CommandType type = CommandType.parseCommand(commandName);
                if (type == CommandType.UNKNOWN) {
                    System.err.println("Ошибка в скрипте (строка " + lineNumber + "): неизвестная команда '" + commandName + "'");
                    scriptDepth--;
                    return new CommandResponse(false,
                            "Скрипт прерван: неизвестная команда '" + commandName + "' на строке " + lineNumber);
                }

                if (!validationManager.isCommandAvailable(type)) {
                    System.err.println("Ошибка в скрипте (строка " + lineNumber + "): команда '" + commandName + "' недоступна на клиенте");
                    scriptDepth--;
                    return new CommandResponse(false,
                            "Скрипт прерван: команда '" + commandName + "' недоступна на клиенте");
                }

                String[] argsArray = commandArgs.isEmpty() ? new String[0] : commandArgs.split("\\s+");
                if (!validationManager.validateArguments(type, argsArray)) {
                    System.err.println("Ошибка в скрипте (строка " + lineNumber + "): неверные аргументы для команды '" + commandName + "'");
                    scriptDepth--;
                    return new CommandResponse(false,
                            "Скрипт прерван: неверные аргументы для команды '" + commandName + "' на строке " + lineNumber);
                }

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

                CommandRequest request;
                if (city != null) {
                    request = new CommandRequest(type, argsArray, city, login, password);
                } else {
                    request = inputManager.createRequest(type, commandArgs, login, password);
                    if (request != null) {
                        request = new CommandRequest(request.getType(), request.getArguments(), request.getCity(), login, password);
                    }
                }

                if (request == null) {
                    System.err.println("Ошибка в скрипте (строка " + lineNumber + "): не удалось создать запрос для команды '" + commandName + "'");
                    scriptDepth--;
                    return new CommandResponse(false,
                            "Скрипт прерван: ошибка при создании запроса для команды '" + commandName + "'");
                }

                CommandResponse response = networkManager.sendRequest(socket, request, host, port);

                if (response == null) {
                    System.err.println("Ошибка в скрипте (строка " + lineNumber + "): сервер не ответил");
                    scriptDepth--;
                    return new CommandResponse(false,
                            "Скрипт прерван: сервер не ответил на команду '" + commandName + "'");
                }

                if (response.isSuccess()) {
                    System.out.println("[" + lineNumber + "] " + commandName + ": " + response.getMessage());

                    if (response.hasData() && response.getData() != null) {
                        System.out.println("=== Все элементы коллекции ===");
                        System.out.println("ID | Название | Координаты | Дата создания | Площадь | Население | Высота | Климат | Правительство | Уровень жизни | Губернатор");

                        for (City c : response.getData()) {
                            System.out.println(c.toString());
                        }

                        System.out.println("================================");
                        System.out.println("Всего элементов: " + response.getData().size());
                    }
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
            scriptDepth = 0;
            return new CommandResponse(false, "Ошибка при чтении файла скрипта: " + e.getMessage());
        } catch (Exception e) {
            scriptDepth = 0;
            return new CommandResponse(false, "Неожиданная ошибка при выполнении скрипта: " + e.getMessage());
        }
    }
}
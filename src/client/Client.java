package client;

import client.managers.InputManager;
import client.managers.NetworkManager;
import client.managers.ValidationManager;
import client.commands.*;
import common.CommandRequest;
import common.CommandResponse;
import common.CommandType;
import common.City;

import java.net.DatagramSocket;
import java.util.NoSuchElementException;
import java.util.Scanner;

/**
 * Клиентское приложение для взаимодействия с сервером коллекции City.
 * Использует блокирующий DatagramSocket для отправки запросов.
 * <p>
 * Обязанности клиента:
 * <ul>
 *   <li>Чтение команд из консоли</li>
 *   <li>Валидация вводимых данных</li>
 *   <li>Сериализация команды и отправка на сервер</li>
 *   <li>Обработка и вывод ответа от сервера</li>
 *   <li>Корректная обработка недоступности сервера</li>
 *   <li>Обработка Ctrl+D (EOF) для корректного завершения</li>
 * </ul>
 * </p>
 * <p>
 * Команда save недоступна на клиенте.
 * Команда exit завершает работу только клиента.
 * </p>
 *
 * @author Максим
 */
public class Client {
    private static String serverHost = "localhost";  // Убрали final для настройки через аргументы
    private static int serverPort = 8080;            // Убрали final для настройки через аргументы
    private static final int TIMEOUT_MS = 5000;      // Исправили таймаут (было 5000*1000 = 83 минуты!)

    private DatagramSocket socket;
    private InputManager inputManager;
    private ValidationManager validationManager;
    private NetworkManager networkManager;
    private boolean isRunning;
    private boolean isServerAvailable;  // Для отслеживания доступности сервера

    public Client() {
        this.inputManager = new InputManager();
        this.validationManager = new ValidationManager();
        this.networkManager = new NetworkManager();
        this.isRunning = true;
        this.isServerAvailable = true;
    }

    /**
     * Запускает клиентское приложение.
     */
    public void start() {
        System.out.println("=== Клиент коллекции City ===");
        System.out.println("Сервер: " + serverHost + ":" + serverPort);
        System.out.println("Введите 'help' для справки, 'exit' для выхода");
        System.out.println();

        try {
            socket = new DatagramSocket();
            socket.setSoTimeout(TIMEOUT_MS);

            if (!connectToServer()) {
                System.err.println("Не удалось подключиться к серверу.");
                System.out.println("Запуск в режиме локальных команд...");
            }

            runMainLoop();

        } catch (Exception e) {
            System.err.println("Ошибка при запуске клиента: " + e.getMessage());
        } finally {
            shutdown();
        }
    }

    /**
     * Проверяет подключение к серверу.
     */
    private boolean connectToServer() {
        System.out.println("Подключение к серверу " + serverHost + ":" + serverPort + "...");

        try {
            CommandRequest testRequest = new CommandRequest(CommandType.INFO, new String[0], null);
            CommandResponse testResponse = networkManager.sendRequest(socket, testRequest, serverHost, serverPort);

            if (testResponse != null) {
                System.out.println("Подключение успешно!");
                isServerAvailable = true;
                return true;
            }
        } catch (Exception e) {
            System.out.println("Ошибка подключения: " + e.getMessage());
        }

        isServerAvailable = false;
        return false;
    }

    /**
     * Пытается переподключиться к серверу.
     */
    private boolean reconnect() {
        System.out.println("Попытка переподключения к серверу...");

        for (int i = 1; i <= 5; i++) {
            try {
                Thread.sleep(2000 * i);
                System.out.println("Попытка " + i + " из 5...");

                if (connectToServer()) {
                    System.out.println("Переподключение успешно!");
                    return true;
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }

        System.err.println("Не удалось подключиться к серверу после 5 попыток");
        return false;
    }

    /**
     * Основной цикл обработки команд.
     */
    private void runMainLoop() {
        Scanner scanner = new Scanner(System.in);

        while (isRunning) {
            try {
                System.out.print("\nВведите команду: ");

                // ОБРАБОТКА Ctrl+D (EOF) — проверка перед чтением
                if (!scanner.hasNextLine()) {
                    System.out.println("\n(Ctrl + D) обнаружен. Завершение работы клиента...");
                    break;
                }

                String line = scanner.nextLine().trim();

                if (line.isEmpty()) continue;

                String[] tokens = line.split("\\s+", 2);
                String commandName = tokens[0].toLowerCase();
                String commandArgs = tokens.length > 1 ? tokens[1] : "";

                CommandType type = CommandType.parseCommand(commandName);
                if (type == CommandType.UNKNOWN) {
                    System.out.println("Неизвестная команда. Введите 'help' для справки.");
                    continue;
                }

                // ПРОВЕРКА: команды, которые НЕ должны иметь аргументов
                if (!commandArgs.isEmpty() && !hasArgumentsAllowed(type)) {
                    System.err.println("Ошибка: команда '" + commandName + "' не принимает аргументы");
                    System.out.println("Правильное использование: " + commandName);
                    continue;
                }

                // ПРОВЕРКА: команды, которые ОБЯЗАНЫ иметь аргументы
                if (commandArgs.isEmpty() && requiresArguments(type)) {
                    System.err.println("Ошибка: команда '" + commandName + "' требует аргумент");
                    printCommandUsage(type);
                    continue;
                }

                // Обработка execute_script
                if (type == CommandType.EXECUTE_SCRIPT) {
                    ExecuteScript scriptCommand = new ExecuteScript();
                    CommandResponse response = scriptCommand.execute(
                            socket, networkManager, serverHost, serverPort, commandArgs);

                    if (response != null) {
                        if (response.isSuccess()) {
                            System.out.println(response.getMessage());
                        } else {
                            System.err.println("Ошибка: " + response.getMessage());
                        }
                    }
                    continue;
                }

                // Обработка exit — БЕЗ System.exit(0)!
                if (type == CommandType.EXIT) {
                    System.out.println("Завершение работы клиента...");
                    break;  // Просто выходим из цикла
                }

                // Блокировка save на клиенте
                if (type == CommandType.SAVE) {
                    System.out.println("Команда 'save' доступна только на сервере!");
                    continue;
                }

                // Проверка доступности сервера для команд, требующих соединения
                if (!isServerAvailable && type != CommandType.HELP && type != CommandType.EXIT) {
                    System.out.println("Сервер недоступен. Команда '" + commandName + "' не может быть выполнена.");
                    System.out.println("Доступны только команды: help, exit");
                    continue;
                }

                // Обработка help (может работать локально)
                if (type == CommandType.HELP) {
                    if (isServerAvailable) {
                        Help helpCommand = new Help();
                        CommandResponse response = helpCommand.execute(socket, networkManager, serverHost, serverPort);
                        handleResponse(response);
                    } else {
                        // Локальная справка
                        Help helpCommand = new Help();
                        System.out.println(helpCommand.getLocalHelp());
                    }
                    continue;
                }

                // Обработка add
                if (type == CommandType.ADD) {
                    Add addCommand = new Add();
                    CommandResponse response = addCommand.execute(socket, networkManager, serverHost, serverPort);
                    handleResponse(response);
                    continue;
                }

                // Обработка info
                if (type == CommandType.INFO) {
                    Info infoCommand = new Info();
                    CommandResponse response = infoCommand.execute(socket, networkManager, serverHost, serverPort);
                    if (response != null) {
                        if (response.isSuccess()) {
                            System.out.println(response.getMessage());
                        } else {
                            System.err.println("Ошибка: " + response.getMessage());
                        }
                    }
                    continue;
                }

                // Обработка show
                if (type == CommandType.SHOW) {
                    Show showCommand = new Show();
                    CommandResponse response = showCommand.execute(socket, networkManager, serverHost, serverPort);
                    if (response != null) {
                        if (response.isSuccess()) {
                            System.out.println(response.getMessage());
                            if (response.hasData() && response.getData() != null) {
                                System.out.println("=== Все элементы коллекции ===");
                                System.out.println("ID | Название | Координаты | Дата создания | Площадь | Население | Высота | Климат | Правительство | Уровень жизни | Губернатор");
                                for (City city : response.getData()) {
                                    System.out.println(city.toString());
                                }
                                System.out.println("================================");
                                System.out.println("Всего элементов: " + response.getData().size());
                            }
                        } else {
                            System.err.println("Ошибка: " + response.getMessage());
                        }
                    }
                    continue;
                }

                // Обработка clear
                if (type == CommandType.CLEAR) {
                    Clear clearCommand = new Clear();
                    CommandResponse response = clearCommand.execute(socket, networkManager, serverHost, serverPort);
                    handleResponse(response);
                    continue;
                }

                // Обработка history
                if (type == CommandType.HISTORY) {
                    History historyCommand = new History();
                    CommandResponse response = historyCommand.execute(socket, networkManager, serverHost, serverPort);
                    if (response != null) {
                        if (response.isSuccess()) {
                            System.out.println(response.getMessage());
                        } else {
                            System.err.println("Ошибка: " + response.getMessage());
                        }
                    }
                    continue;
                }

                // Обработка sum_of_meters_above_sea_level
                if (type == CommandType.SUM_OF_METERS_ABOVE_SEA_LEVEL) {
                    SumOfMeters sumCommand = new SumOfMeters();
                    CommandResponse response = sumCommand.execute(socket, networkManager, serverHost, serverPort);
                    if (response != null) {
                        if (response.isSuccess()) {
                            System.out.println(response.getMessage());
                        } else {
                            System.err.println("Ошибка: " + response.getMessage());
                        }
                    }
                    continue;
                }

                // Обработка min_by_coordinates
                if (type == CommandType.MIN_BY_COORDINATES) {
                    MinByCoordinates minCommand = new MinByCoordinates();
                    CommandResponse response = minCommand.execute(socket, networkManager, serverHost, serverPort);
                    if (response != null) {
                        if (response.isSuccess()) {
                            System.out.println(response.getMessage());
                        } else {
                            System.err.println("Ошибка: " + response.getMessage());
                        }
                    }
                    continue;
                }

                // Обработка print_unique_governor
                if (type == CommandType.PRINT_UNIQUE_GOVERNOR) {
                    PrintUniqueGovernor govCommand = new PrintUniqueGovernor();
                    CommandResponse response = govCommand.execute(socket, networkManager, serverHost, serverPort);
                    if (response != null) {
                        if (response.isSuccess()) {
                            System.out.println(response.getMessage());
                        } else {
                            System.err.println("Ошибка: " + response.getMessage());
                        }
                    }
                    continue;
                }

                // Обработка remove_first
                if (type == CommandType.REMOVE_FIRST) {
                    RemoveFirst removeCommand = new RemoveFirst();
                    CommandResponse response = removeCommand.execute(socket, networkManager, serverHost, serverPort);
                    if (response != null) {
                        if (response.isSuccess()) {
                            System.out.println(response.getMessage());
                        } else {
                            System.err.println("Ошибка: " + response.getMessage());
                        }
                    }
                    continue;
                }

                // Обработка remove_lower
                if (type == CommandType.REMOVE_LOWER) {
                    RemoveLower removeCommand = new RemoveLower();
                    CommandResponse response = removeCommand.execute(socket, networkManager, serverHost, serverPort);
                    if (response != null) {
                        if (response.isSuccess()) {
                            System.out.println(response.getMessage());
                        } else {
                            System.err.println("Ошибка: " + response.getMessage());
                        }
                    }
                    continue;
                }

                // Обработка remove_by_id
                if (type == CommandType.REMOVE_BY_ID) {
                    RemoveById removeCommand = new RemoveById();
                    CommandResponse response = removeCommand.executeWithArgs(
                            socket, networkManager, serverHost, serverPort, commandArgs.split("\\s+"));

                    if (response != null) {
                        if (response.isSuccess()) {
                            System.out.println(response.getMessage());
                        } else {
                            System.err.println("Ошибка: " + response.getMessage());
                        }
                    }
                    continue;
                }

                // Обработка update
                if (type == CommandType.UPDATE) {
                    Update updateCommand = new Update();
                    CommandResponse response = updateCommand.executeWithArgs(
                            socket, networkManager, serverHost, serverPort, commandArgs.split("\\s+"));

                    if (response != null) {
                        if (response.isSuccess()) {
                            System.out.println(response.getMessage());
                        } else {
                            System.err.println("Ошибка: " + response.getMessage());
                        }
                    }
                    continue;
                }

                // Для остальных команд
                CommandRequest request = inputManager.createRequest(type, commandArgs);
                if (request == null) continue;

                System.out.println("Отправка запроса на сервер...");
                CommandResponse response = networkManager.sendRequest(socket, request, serverHost, serverPort);

                // Обработка отключения сервера
                if (response == null) {
                    System.err.println("Сервер не ответил (таймаут " + TIMEOUT_MS + " мс)");

                    if (isServerAvailable) {
                        System.out.println("Сервер стал недоступен. Попытка переподключения...");
                        isServerAvailable = false;

                        if (!reconnect()) {
                            System.out.println("Переход в режим локальных команд...");
                            System.out.println("Доступны команды: help, exit");
                        }
                    }
                    continue;
                }

                // Восстановление соединения
                if (!isServerAvailable) {
                    System.out.println("Соединение с сервером восстановлено!");
                    isServerAvailable = true;
                }

                handleResponse(response);

            } catch (NoSuchElementException e) {
                // ALSO ловим Ctrl+D через исключение
                System.out.println("\n(Ctrl + D) обнаружен. Завершение работы клиента...");
                break;
            } catch (Exception e) {
                System.err.println("Ошибка: " + e.getMessage());
            }
        }
    }

    /**
     * Проверяет, разрешены ли аргументы для данной команды.
     *
     * @param type тип команды
     * @return true если аргументы разрешены
     */
    private boolean hasArgumentsAllowed(CommandType type) {
        return switch (type) {
            case REMOVE_BY_ID, UPDATE, EXECUTE_SCRIPT -> true;
            case ADD, REMOVE_LOWER -> true;  // Аргументы не нужны, но нужен ввод City
            default -> false;
        };
    }

    /**
     * Проверяет, требуются ли аргументы для данной команды.
     *
     * @param type тип команды
     * @return true если аргументы обязательны
     */
    private boolean requiresArguments(CommandType type) {
        return switch (type) {
            case REMOVE_BY_ID, UPDATE, EXECUTE_SCRIPT -> true;
            default -> false;
        };
    }

    /**
     * Выводит правильное использование команды.
     *
     * @param type тип команды
     */
    private void printCommandUsage(CommandType type) {
        switch (type) {
            case REMOVE_BY_ID -> System.out.println("Пример: remove_by_id 5");
            case UPDATE -> System.out.println("Пример: update 5");
            case EXECUTE_SCRIPT -> System.out.println("Пример: execute_script script.txt");
            default -> System.out.println("Использование: " + type.name().toLowerCase());
        }
    }

    /**
     * Обрабатывает ответ от сервера.
     */
    private void handleResponse(CommandResponse response) {
        if (response == null) {
            System.err.println("Сервер не ответил");
            return;
        }

        if (response.isSuccess()) {
            System.out.println(response.getMessage());

            if (response.hasData() && response.getData() != null) {
                System.out.println("=== Все элементы коллекции ===");
                System.out.println("ID | Название | Координаты | Дата создания | Площадь | Население | Высота | Климат | Правительство | Уровень жизни | Губернатор");

                for (City city : response.getData()) {
                    System.out.println(city.toString());
                }

                System.out.println("================================");
                System.out.println("Всего элементов: " + response.getData().size());
            }
        } else {
            System.err.println("Ошибка: " + response.getMessage());
        }
    }

    /**
     * Завершает работу клиента.
     */
    private void shutdown() {
        if (socket != null && !socket.isClosed()) {
            socket.close();
        }
        isRunning = false;
        System.out.println("Клиент остановлен");
    }

    /**
     * Точка входа в клиентское приложение.
     */
    public static void main(String[] args) {
        // Обработка аргументов командной строки
        if (args.length >= 1) {
            serverHost = args[0];
        }
        if (args.length >= 2) {
            try {
                serverPort = Integer.parseInt(args[1]);
            } catch (NumberFormatException e) {
                System.err.println("Неверный формат порта. Используется порт по умолчанию: 8080");
            }
        }

        System.out.println("Подключение к серверу: " + serverHost + ":" + serverPort);

        Client client = new Client();
        client.start();
    }
}
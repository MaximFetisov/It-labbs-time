package client;

import client.commands.*;
import client.managers.InputManager;
import client.managers.NetworkManager;
import client.managers.ValidationManager;
import common.CommandRequest;
import common.CommandResponse;
import common.CommandType;
import common.City;

import java.net.DatagramSocket;
import java.util.NoSuchElementException;
import java.util.Scanner;

/**
 * Клиентское приложение для взаимодействия с сервером коллекции City.
 * Поддерживает авторизацию пользователей.
 */
public class Client {
    private static String serverHost = "localhost";
    private static int serverPort = 8080;
    private static final int TIMEOUT_MS = 5000;

    private DatagramSocket socket;
    private InputManager inputManager;
    private ValidationManager validationManager;
    private NetworkManager networkManager;
    private boolean isRunning;
    private boolean isServerAvailable;

    private String currentLogin = null;
    private String currentPassword = null;

    public Client() {
        this.inputManager = new InputManager();
        this.validationManager = new ValidationManager();
        this.networkManager = new NetworkManager();
        this.isRunning = true;
        this.isServerAvailable = true;
    }

    public void start() {
        System.out.println("=== Клиент коллекции City ===");
        System.out.println("Сервер: " + serverHost + ":" + serverPort);
        System.out.println("Введите 'help' для справки, 'exit' для выхода\n");

        try {
            socket = new DatagramSocket();
            socket.setSoTimeout(TIMEOUT_MS);

            if (!connectToServer()) {
                System.err.println("Не удалось подключиться к серверу.");
                System.out.println("Запуск в режиме локальных команд...");
            }

            if (isServerAvailable && !authenticate()) {
                System.out.println("Авторизация отменена. Завершение работы клиента.");
                shutdown();
                return;
            }

            runMainLoop();

        } catch (Exception e) {
            System.err.println("Ошибка при запуске: " + e.getMessage());
        } finally {
            shutdown();
        }
    }

    private boolean authenticate() {
        Scanner authScanner = new Scanner(System.in);

        while (true) {
            System.out.println("\n=== Авторизация ===");
            System.out.println("1. Войти (l) | 2. Зарегистрироваться (r) | 3. Выход (e)");

            if (!authScanner.hasNextLine()) {
                System.out.println("\nЗавершение работы...");
                return false;
            }

            String choice = authScanner.nextLine().trim().toLowerCase();

            if (choice.equals("e")) {
                return false;
            }

            if (!choice.equals("l") && !choice.equals("r")) {
                System.out.println("Неверный выбор. Введите 'l', 'r' или 'e'");
                continue;
            }

            System.out.print("Логин, 4 и более символов: ");
            if (!authScanner.hasNextLine()) return false;
            String login = authScanner.nextLine().trim();

            System.out.print("Пароль, 6 и более символов: ");
            if (!authScanner.hasNextLine()) return false;
            String password = authScanner.nextLine().trim();

            if (login.length() < 4) {
                System.out.println("Ошибка: логин должен содержать не менее 4 символов");
                continue;
            }
            if (password.length() < 6) {
                System.out.println("Ошибка: пароль должен содержать не менее 6 символов");
                continue;
            }

            CommandType type = choice.equals("l") ? CommandType.LOGIN : CommandType.REGISTER;
            CommandRequest req = new CommandRequest(type, new String[0], null, login, password);

            CommandResponse resp = networkManager.sendRequest(socket, req, serverHost, serverPort);

            if (resp != null && resp.isSuccess()) {
                System.out.println(resp.getMessage());
                this.currentLogin = login;
                this.currentPassword = password;
                return true;
            } else {
                System.out.println("Ошибка: " + (resp != null ? resp.getMessage() : "Сервер не ответил"));
            }
        }
    }

    private boolean connectToServer() {
        System.out.println("Подключение к серверу " + serverHost + ":" + serverPort + "...");
        try {
            CommandRequest testRequest = new CommandRequest(CommandType.INFO, new String[0], null, null, null);
            CommandResponse testResponse = networkManager.sendRequest(socket, testRequest, serverHost, serverPort);
            if (testResponse != null) {
                isServerAvailable = true;
                System.out.println("Подключение успешно!");
                return true;
            }
        } catch (Exception e) {
            System.out.println("Ошибка подключения: " + e.getMessage());
        }
        isServerAvailable = false;
        return false;
    }

    private boolean reconnect() {
        System.out.println("Попытка переподключения к серверу...");
        for (int i = 1; i <= 5; i++) {
            try {
                Thread.sleep(2000 * i);
                System.out.println("Попытка " + i + " из 5...");
                if (connectToServer()) return true;
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
        System.err.println("Не удалось подключиться после 5 попыток");
        return false;
    }

    private void runMainLoop() {
        Scanner scanner = new Scanner(System.in);

        while (isRunning) {
            try {
                System.out.print("Введите команду: ");
                if (!scanner.hasNextLine()) {
                    System.out.println("\n(Ctrl + D) обнаружен. Завершение...");
                    break;
                }

                String line = scanner.nextLine().trim();
                if (line.isEmpty()) continue;

                String[] tokens = line.split("\\s+", 2);
                String commandName = tokens[0].toLowerCase();
                String commandArgs = tokens.length > 1 ? tokens[1] : "";

                CommandType type = CommandType.parseCommand(commandName);
                if (type == CommandType.UNKNOWN) {
                    System.out.println("Неизвестная команда. Введите 'help'.");
                    continue;
                }

                if (!commandArgs.isEmpty() && !hasArgumentsAllowed(type)) {
                    System.err.println("Ошибка: команда '" + commandName + "' не принимает аргументы");
                    continue;
                }
                if (commandArgs.isEmpty() && requiresArguments(type)) {
                    System.err.println("Ошибка: команда '" + commandName + "' требует аргумент");
                    continue;
                }

                dispatchCommand(type, commandArgs);

            } catch (NoSuchElementException e) {
                System.out.println("\n(Ctrl + D) обнаружен. Завершение...");
                break;
            } catch (Exception e) {
                System.err.println("Ошибка: " + e.getMessage());
            }
        }
    }

    private void dispatchCommand(CommandType type, String args) {
        switch (type) {
            case EXIT -> {
                System.out.println("Завершение работы...");
                isRunning = false;
            }
            case SAVE -> System.out.println("Команда 'save' доступна только на сервере!");
            case EXECUTE_SCRIPT -> {
                ExecuteScript script = new ExecuteScript();
                handleResponse(script.execute(socket, networkManager, serverHost, serverPort, args, currentLogin, currentPassword));
            }
            case UPDATE -> {
                long id = Long.parseLong(args.split("\\s+")[0]);
                handleResponse(new Update().execute(socket, networkManager, serverHost, serverPort, id, currentLogin, currentPassword));
            }
            case REMOVE_BY_ID -> {
                RemoveById cmd = new RemoveById();
                handleResponse(cmd.executeWithArgs(socket, networkManager, serverHost, serverPort, args.split("\\s+"), currentLogin, currentPassword));
            }
            case HELP -> {
                Help cmd = new Help();
                handleResponse(isServerAvailable ? cmd.execute(socket, networkManager, serverHost, serverPort, currentLogin, currentPassword)
                        : new CommandResponse(true, cmd.getLocalHelp()));
            }
            case LOGIN -> {
                System.out.println("Вы уже авторизованы как: " + currentLogin);
                System.out.print("Выйти из аккаунта? (yes/no): ");
                Scanner sc = new Scanner(System.in);
                String ans = sc.nextLine().trim().toLowerCase();
                if (ans.equals("yes") || ans.equals("y")) {
                    currentLogin = null;
                    currentPassword = null;
                    authenticate();
                }
            }
            case REGISTER -> {
                System.out.println("Вы уже авторизованы. Сначала выйдите из аккаунта (logout).");
            }
            case LOGOUT -> {
                CommandRequest req = new CommandRequest(CommandType.LOGOUT, new String[0], null, currentLogin, currentPassword);
                CommandResponse resp = networkManager.sendRequest(socket, req, serverHost, serverPort);
                if (resp != null && resp.isSuccess()) {
                    System.out.println(resp.getMessage());
                    currentLogin = null;
                    currentPassword = null;
                    System.out.println("Возврат к экрану авторизации...");
                    authenticate();
                } else {
                    System.err.println("Ошибка выхода: " + (resp != null ? resp.getMessage() : "Сервер не ответил"));
                }
            }
            case ADD -> handleResponse(new Add().execute(socket, networkManager, serverHost, serverPort, currentLogin, currentPassword));
            case INFO -> handleResponse(new Info().execute(socket, networkManager, serverHost, serverPort, currentLogin, currentPassword));
            case SHOW -> handleResponse(new Show().execute(socket, networkManager, serverHost, serverPort, currentLogin, currentPassword));
            case CLEAR -> handleResponse(new Clear().execute(socket, networkManager, serverHost, serverPort, currentLogin, currentPassword));
            case HISTORY -> handleResponse(new History().execute(socket, networkManager, serverHost, serverPort, currentLogin, currentPassword));
            case SUM_OF_METERS_ABOVE_SEA_LEVEL -> handleResponse(new SumOfMeters().execute(socket, networkManager, serverHost, serverPort, currentLogin, currentPassword));
            case MIN_BY_COORDINATES -> handleResponse(new MinByCoordinates().execute(socket, networkManager, serverHost, serverPort, currentLogin, currentPassword));
            case PRINT_UNIQUE_GOVERNOR -> handleResponse(new PrintUniqueGovernor().execute(socket, networkManager, serverHost, serverPort, currentLogin, currentPassword));
            case REMOVE_FIRST -> handleResponse(new RemoveFirst().execute(socket, networkManager, serverHost, serverPort, currentLogin, currentPassword));
            case REMOVE_LOWER -> handleResponse(new RemoveLower().execute(socket, networkManager, serverHost, serverPort, currentLogin, currentPassword));
            case NEVER -> new Never().execute(socket, networkManager, serverHost, serverPort, currentLogin, currentPassword);
            default -> System.out.println("Команда не реализована.");
        }
    }

    private void handleResponse(CommandResponse response) {
        if (response == null) {
            System.err.println("Сервер не ответил (таймаут).");
            if (isServerAvailable) {
                System.out.println("Попытка переподключения...");
                isServerAvailable = false;
                if (!reconnect()) {
                    System.out.println("Переход в локальный режим (help, exit).");
                }
            }
            return;
        }

        if (!response.isSuccess() && response.getMessage() != null &&
                (response.getMessage().contains("авторизации") || response.getMessage().contains("пароль"))) {
            System.out.println("\nОшибка авторизации. Хотите перезайти? (yes/no)");
            Scanner sc = new Scanner(System.in);
            String ans = sc.nextLine().trim().toLowerCase();
            if (ans.equals("yes") || ans.equals("y")) {
                currentLogin = null;
                currentPassword = null;
                authenticate();
            }
            return;
        }

        if (!isServerAvailable) {
            System.out.println("Соединение восстановлено!");
            isServerAvailable = true;
        }

        if (response.isSuccess()) {
            System.out.println(response.getMessage());
            if (response.hasData() && response.getData() != null) {
                System.out.println("=== Коллекция ===");
                response.getData().forEach(city -> System.out.println(city));
            }
        } else {
            System.err.println("Ошибка сервера: " + response.getMessage());
        }
    }

    private boolean hasArgumentsAllowed(CommandType type) {
        return switch (type) {
            case REMOVE_BY_ID, UPDATE, EXECUTE_SCRIPT -> true;
            case ADD, REMOVE_LOWER -> true;
            default -> false;
        };
    }

    private boolean requiresArguments(CommandType type) {
        return switch (type) {
            case REMOVE_BY_ID, UPDATE, EXECUTE_SCRIPT -> true;
            default -> false;
        };
    }

    private void shutdown() {
        if (socket != null && !socket.isClosed()) socket.close();
        isRunning = false;
        System.out.println("Клиент остановлен.");
    }

    public static void main(String[] args) {
        if (args.length >= 1) serverHost = args[0];
        if (args.length >= 2) {
            try { serverPort = Integer.parseInt(args[1]); }
            catch (NumberFormatException e) { System.err.println("Неверный порт."); }
        }
        new Client().start();
    }
}
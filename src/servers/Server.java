package servers;

import common.CommandRequest;
import common.CommandResponse;
import common.CommandType;
import common.City;
import servers.modules.RequestModule;
import servers.modules.CommandModule;
import servers.modules.ResponseModule;
import servers.managers.CollectionManager;
import servers.managers.FileManager;

import java.io.IOException;
import java.net.BindException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.nio.channels.Selector;
import java.nio.channels.SelectionKey;
import java.util.Scanner;
import java.util.Set;
import java.util.logging.Logger;
import java.util.logging.FileHandler;
import java.util.logging.SimpleFormatter;
import java.util.NoSuchElementException;

/**
 * Серверное приложение для управления коллекцией городов.
 * Работает по протоколу UDP в однопоточном режиме с использованием Selector.
 */
public class Server {
    private static int PORT = 8080;  // Убрали final для настройки через аргументы
    private static final int BUFFER_SIZE = 65535;
    private static final Logger logger = Logger.getLogger(Server.class.getName());

    private DatagramChannel channel;
    private Selector selector;  // Добавлено поле Selector
    private CollectionManager collectionManager;
    private FileManager fileManager;
    private String dataFile;
    private boolean isRunning;

    /**
     * Создаёт и инициализирует сервер.
     *
     * @param dataFile путь к файлу данных для загрузки коллекции
     */
    public Server(String dataFile) {
        this.dataFile = dataFile;
        this.collectionManager = new CollectionManager();
        this.fileManager = new FileManager();
        this.isRunning = true;
        setupLogger();
    }

    /**
     * Настраивает логирование через FileHandler.
     */
    private void setupLogger() {
        try {
            new java.io.File("logs").mkdirs();

            FileHandler fileHandler = new FileHandler("logs/server.log", true);
            fileHandler.setFormatter(new SimpleFormatter());
            logger.addHandler(fileHandler);
            logger.setUseParentHandlers(false);
        } catch (IOException e) {
            System.err.println("Не удалось настроить логирование: " + e.getMessage());
        }
    }

    /**
     * Запускает сервер в однопоточном режиме.
     */
    public void start() {
        System.out.println("=== Запуск сервера ===");
        logger.info("Порт: " + PORT);
        logger.info("Файл данных: " + dataFile);

        try {
            channel = DatagramChannel.open();
            channel.configureBlocking(false);

            // Обработка занятого порта
            try {
                channel.bind(new InetSocketAddress(PORT));
            } catch (BindException e) {
                logger.severe("Порт " + PORT + " уже занят!");
                System.err.println("Ошибка: порт " + PORT + " уже используется другим сервером");
                System.err.println("Решение: укажите другой порт через аргумент: java servers.Server <порт> <файл>");
                return;  // Корректное завершение, не краш
            }

            // Открытие и регистрация Selector
            selector = Selector.open();
            channel.register(selector, SelectionKey.OP_READ);

            logger.info("Сервер запущен и ожидает подключения на порту " + PORT);

            loadCollection();
            startServerConsole();
            Runtime.getRuntime().addShutdownHook(new Thread(this::shutdown));
            runMainLoop();

        } catch (IOException e) {
            logger.severe("Ошибка при запуске сервера: " + e.getMessage());
            System.err.println("Критическая ошибка сервера: " + e.getMessage());
        }
    }

    /**
     * Запускает поток для обработки команд с консоли сервера.
     */
    private void startServerConsole() {
        new Thread(() -> {
            Scanner scanner = new Scanner(System.in);
            try {
                System.out.println("=== Консоль сервера ===");
                System.out.println("Введите 'help' для справки, 'save' для сохранения, 'exit' для выхода");
                System.out.println("Нажмите Ctrl+D для завершения сервера");
                System.out.println();

                while (isRunning) {
                    System.out.print("server> ");

                    // ОБРАБОТКА Ctrl+D (EOF) — проверка перед чтением
                    if (!scanner.hasNextLine()) {
                        System.out.println("\n(Ctrl + D) обнаружен. Завершение работы сервера...");
                        stop();
                        break;
                    }

                    String input = scanner.nextLine().trim();

                    if (input.equalsIgnoreCase("save")) {
                        logger.info("Экстренное сохранение коллекции...");
                        try {
                            fileManager.writeToJsonFile(dataFile, collectionManager.getCities());
                            logger.info("Коллекция успешно сохранена в: " + dataFile);
                            System.out.println("Коллекция успешно сохранена");
                        } catch (Exception e) {
                            logger.severe("Ошибка при сохранении: " + e.getMessage());
                            System.out.println("Ошибка при сохранении: " + e.getMessage());
                        }
                    } else if (input.equalsIgnoreCase("exit")) {
                        logger.info("Завершение работы сервера по команде пользователя...");
                        System.out.println("Завершение работы сервера...");
                        stop();
                        break;
                    } else if (input.equalsIgnoreCase("help")) {
                        System.out.println("=== Справка ===");
                        System.out.println("save - сохранить коллекцию в файл");
                        System.out.println("exit - завершить работу сервера");
                        System.out.println("help - показать эту справку");
                        System.out.println("===============");
                    } else if (input.isEmpty()) {
                        continue;
                    } else {
                        System.out.println("Неизвестная команда. Введите 'help' для справки.");
                    }
                }
            } catch (NoSuchElementException e) {
                // ALSO ловим Ctrl+D через исключение
                System.out.println("\n(Ctrl + D) обнаружен. Завершение работы сервера...");
                logger.info("Сервер завершён через Ctrl+D");
                stop();
            } catch (Exception e) {
                logger.severe("Ошибка в консоли сервера: " + e.getMessage());
            } finally {
                scanner.close();
            }
        }).start();
    }

    /**
     * Загружает коллекцию из файла при старте сервера.
     */
    private void loadCollection() {
        logger.info("Загрузка коллекции из файла: " + dataFile);

        collectionManager.setCities(fileManager.readFromJsonFile(dataFile, City.class));

        if (!collectionManager.getCities().isEmpty()) {
            long maxId = collectionManager.getCities().stream()
                    .mapToLong(City::getId)
                    .max()
                    .orElse(0L);
            collectionManager.setNextId(maxId + 1);
        }

        logger.info("Загружено элементов: " + collectionManager.getSize());
    }

    /**
     * Основной цикл обработки запросов клиентов с использованием
     */
    private void runMainLoop() {
        ByteBuffer buffer = ByteBuffer.allocate(BUFFER_SIZE);

        while (isRunning) {
            try {
                // Ожидание событий через Selector
                selector.select(100);  // Таймаут 100 мс

                Set<SelectionKey> selectedKeys = selector.selectedKeys();

                for (SelectionKey key : selectedKeys) {
                    if (key.isReadable()) {
                        // Приём данных от клиента
                        InetSocketAddress clientAddress = (InetSocketAddress) channel.receive(buffer);

                        if (clientAddress != null) {
                            logger.info("Получен запрос от: " + clientAddress);

                            buffer.flip();

                            byte[] data = new byte[buffer.remaining()];
                            buffer.get(data);

                            // Десериализация запроса
                            CommandRequest request = RequestModule.deserialize(data);
                            logger.info("Тип команды: " + request.getType());

                            // Обработка команды
                            CommandModule commandModule = new CommandModule(collectionManager, fileManager, dataFile);
                            CommandResponse response = commandModule.execute(request);

                            if (request.getType() != CommandType.EXIT) {
                                collectionManager.addToHistory(request.getType().name());
                            }

                            // Отправка ответа
                            byte[] responseData = ResponseModule.serialize(response);
                            channel.send(ByteBuffer.wrap(responseData), clientAddress);
                            logger.info("Ответ отправлен клиенту " + clientAddress);
                        }

                        buffer.clear();
                    }
                }

                // Обязательно очищаем набор выбранных ключей
                selectedKeys.clear();

            } catch (IOException e) {
                logger.severe("Ошибка сетевого взаимодействия: " + e.getMessage());
            } catch (ClassNotFoundException e) {
                logger.severe("Ошибка десериализации: " + e.getMessage());
            } catch (Exception e) {
                logger.severe("Необработанная ошибка: " + e.getMessage());
            }
        }
    }

    /**
     * Метод завершения работы сервера.
     */
    void shutdown() {
        logger.info("=== Завершение работы сервера ===");

        try {
            fileManager.writeToJsonFile(dataFile, collectionManager.getCities());
            logger.info("Коллекция сохранена в файл: " + dataFile);
        } catch (IOException e) {
            logger.severe("Ошибка при сохранении коллекции: " + e.getMessage());
        } finally {
            try {
                if (channel != null && channel.isOpen()) {
                    channel.close();
                    logger.info("Сетевой канал закрыт");
                }
                // Закрытие Selector
                if (selector != null && selector.isOpen()) {
                    selector.close();
                    logger.info("Selector закрыт");
                }
            } catch (IOException e) {
                logger.warning("Ошибка при закрытии канала: " + e.getMessage());
            }

            isRunning = false;
            logger.info("Сервер остановлен");
        }
    }

    /**
     * Останавливает сервер корректно.
     */
    public void stop() {
        isRunning = false;
    }

    /**
     * Точка входа в серверное приложение.
     */
    public static void main(String[] args) {
        String dataFile = "data/cities.json";

        if (args.length >= 1) {
            try {
                PORT = Integer.parseInt(args[0]);  // Первый аргумент — порт
                if (args.length >= 2) {
                    dataFile = args[1];
                }
            } catch (NumberFormatException e) {
                dataFile = args[0];  // Если первый аргумент не число — это путь к файлу
            }
        }

        System.out.println("Порт: " + PORT);
        System.out.println("Файл данных: " + dataFile);

        new java.io.File("logs").mkdirs();

        Server server = new Server(dataFile);
        server.start();
    }
}
package servers;

import common.CommandRequest;
import common.CommandResponse;
import common.CommandType;
import common.City;
import servers.modules.RequestModule;
import servers.modules.CommandModule;
import servers.modules.RateLimiter;
import servers.modules.ResponseModule;
import servers.managers.CollectionManager;
import servers.managers.DatabaseManager;

import java.io.IOException;
import java.net.BindException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.nio.channels.Selector;
import java.nio.channels.SelectionKey;
import java.util.Scanner;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Logger;
import java.util.logging.FileHandler;
import java.util.logging.SimpleFormatter;
import java.util.NoSuchElementException;

/**
 * Серверное приложение для управления коллекцией городов.
 * Работает по протоколу UDP с использованием Selector и многопоточной обработки.
 * <p>
 * Требования Lab 7:
 * <ul>
 *   <li>Хранение в PostgreSQL (без файлов)</li>
 *   <li>Авторизация пользователей (хеширование MD2)</li>
 *   <li>Права доступа: модификация только своих объектов</li>
 *   <li>CollectionManager — синхронизированный кэш БД</li>
 *   <li>Многопоточность: CachedThreadPool (чтение) → new Thread (логика) → FixedThreadPool (отправка)</li>
 *   <li>Синхронизация: Collections.synchronizedList</li>
 * </ul>
 * </p>
 *
 * @author Максим
 */
public class Server {
    private static int PORT = 8080;
    private static final int BUFFER_SIZE = 65535;
    private static final Logger logger = Logger.getLogger(Server.class.getName());

    private DatagramChannel channel;
    private Selector selector;

    private CollectionManager collectionManager;
    private DatabaseManager databaseManager;
    private String dbConfigFile;

    private boolean isRunning;
    private RateLimiter rateLimiter;

    // Пулы потоков для многопоточности
    private ExecutorService readPool;    // CachedThreadPool — чтение
    private ExecutorService writePool;   // FixedThreadPool — отправка

    public Server(String dbConfigFile) {
        this.dbConfigFile = dbConfigFile;
        this.databaseManager = DatabaseManager.getInstance();
        // CollectionManager создаётся с ссылкой на DatabaseManager для синхронизации
        this.collectionManager = new CollectionManager(databaseManager);
        this.isRunning = true;
        this.rateLimiter = new RateLimiter();
        setupLogger();
        initThreadPools();
    }

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

    private void initThreadPools() {
        // CachedThreadPool для чтения (много кратковременных подключений)
        readPool = Executors.newCachedThreadPool();
        // FixedThreadPool для отправки (ограничиваем нагрузку на сеть)
        writePool = Executors.newFixedThreadPool(10);
        logger.info("Пулы потоков инициализированы");
    }

    public void start() {
        System.out.println("=== Запуск сервера ===");
        logger.info("Порт: " + PORT);
        logger.info("Конфиг БД: " + dbConfigFile);

        try {
            // Инициализация БД (с авто-созданием таблиц)
            if (!databaseManager.init(dbConfigFile)) {
                logger.severe("Не удалось подключиться к БД. Сервер не запущен.");
                System.err.println("Ошибка подключения к БД. Проверьте db.properties");
                return;
            }
            logger.info("БД инициализирована, загрузка коллекции...");

            // Загрузка коллекции из БД в кэш памяти (только при старте!)
            collectionManager.refreshFromDb();
            logger.info("Загружено элементов из БД в кэш: " + collectionManager.getSize());
            System.out.println("Загружено элементов из БД: " + collectionManager.getSize());

            // Настройка UDP + Selector
            channel = DatagramChannel.open();
            channel.configureBlocking(false);

            try {
                channel.bind(new InetSocketAddress(PORT));
                logger.info("Сервер слушает порт " + PORT);
            } catch (BindException e) {
                logger.severe("Порт " + PORT + " уже занят!");
                System.err.println("Ошибка: порт " + PORT + " уже используется");
                return;
            }

            selector = Selector.open();
            channel.register(selector, SelectionKey.OP_READ);

            logger.info("Сервер запущен и готов к приёму запросов");
            System.out.println("Сервер запущен на порту " + PORT);

            startServerConsole();
            Runtime.getRuntime().addShutdownHook(new Thread(this::shutdown));
            runMainLoop();

        } catch (IOException e) {
            logger.severe("Ошибка при запуске: " + e.getMessage());
            e.printStackTrace();
            System.err.println("Критическая ошибка: " + e.getMessage());
        }
    }

    private void startServerConsole() {
        new Thread(() -> {
            Scanner scanner = new Scanner(System.in);
            try {
                System.out.println("=== Консоль сервера ===");
                System.out.println("Команды: help, exit");

                while (isRunning) {
                    System.out.print("server> ");

                    if (!scanner.hasNextLine()) {
                        System.out.println("\n(Ctrl + D) обнаружен. Завершение...");
                        shutdown();
                        break;
                    }

                    String input = scanner.nextLine().trim();

                    if (input.equalsIgnoreCase("exit")) {
                        logger.info("Завершение по команде пользователя");
                        shutdown();
                        break;
                    } else if (input.equalsIgnoreCase("help")) {
                        System.out.println("exit — завершить работу сервера");
                        System.out.println("help — показать справку");
                    } else if (!input.isEmpty()) {
                        System.out.println("Неизвестная команда");
                    }
                }
            } catch (NoSuchElementException e) {
                System.out.println("\n(Ctrl + D) обнаружен. Завершение...");
                shutdown();
            } finally {
                scanner.close();
            }
        }).start();
    }

    private void runMainLoop() {
        ByteBuffer buffer = ByteBuffer.allocate(BUFFER_SIZE);

        while (isRunning) {
            try {
                selector.select(100);
                Set<SelectionKey> selectedKeys = selector.selectedKeys();

                for (SelectionKey key : selectedKeys) {
                    if (key.isReadable()) {
                        InetSocketAddress clientAddress = (InetSocketAddress) channel.receive(buffer);

                        if (clientAddress != null) {
                            logger.fine("Запрос от: " + clientAddress);
                            buffer.flip();

                            byte[] data = new byte[buffer.remaining()];
                            buffer.get(data);

                            // Чтение и десериализация в отдельном потоке из пула
                            readPool.submit(() -> {
                                try {
                                    CommandRequest request = RequestModule.deserialize(data);
                                    logger.info("Команда: " + request.getType());

                                    // Проверка rate limit
                                    if (!rateLimiter.isAllowed(clientAddress.getAddress(), request.getType())) {
                                        logger.warning("Rate limit: " + clientAddress + " / " + request.getType());
                                        CommandResponse rateLimitResp = new CommandResponse(false,
                                                "Превышен лимит для '" + request.getType().name().toLowerCase() + "'");
                                        sendResponse(clientAddress, rateLimitResp);
                                        return;
                                    }

                                    // Логика команды в отдельном потоке
                                    new Thread(() -> {
                                        try {
                                            CommandModule commandModule = new CommandModule(
                                                    collectionManager, databaseManager);
                                            CommandResponse response = commandModule.execute(request);

                                            if (request.getType() != CommandType.EXIT) {
                                                collectionManager.addToHistory(request.getType().name());
                                            }

                                            sendResponse(clientAddress, response);

                                        } catch (Exception e) {
                                            logger.severe("Ошибка обработки: " + e.getMessage());
                                            e.printStackTrace();
                                            sendResponse(clientAddress,
                                                    new CommandResponse(false, "Внутренняя ошибка сервера"));
                                        }
                                    }).start();

                                } catch (Exception e) {
                                    logger.severe("Ошибка чтения: " + e.getMessage());
                                    e.printStackTrace();
                                }
                            });
                        }
                        buffer.clear();
                    }
                }
                selectedKeys.clear();

            } catch (java.nio.channels.ClosedSelectorException e) {
                // Селектор был закрыт (при завершении работы) - это нормальная ситуация
                logger.info("Селектор закрыт. Завершение цикла обработки...");
                break;
            } catch (IOException e) {
                logger.severe("Сетевая ошибка: " + e.getMessage());
                e.printStackTrace();
            } catch (Exception e) {
                logger.severe("Необработанная ошибка: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    private void sendResponse(InetSocketAddress clientAddress, CommandResponse response) {
        writePool.submit(() -> {
            try {
                ResponseModule.sendWithChunks(channel, clientAddress, response);
                logger.fine("Ответ отправлен: " + clientAddress);
            } catch (IOException e) {
                logger.warning("Ошибка отправки: " + e.getMessage());
            }
        });
    }

    void shutdown() {
        isRunning = false; // Сначала устанавливаем флаг
        logger.info("=== Завершение сервера ===");

        try {
            // Экстренное сохранение: синхронизация кэша с БД
            // (на случай, если какие-то изменения не успели записаться)
            databaseManager.syncCollectionToDb(collectionManager.getCities());
            logger.info("Коллекция синхронизирована с БД");
        } catch (Exception e) {
            logger.severe("Ошибка синхронизации: " + e.getMessage());
        } finally {
            try {
                if (channel != null && channel.isOpen()) channel.close();
                if (selector != null && selector.isOpen()) selector.close();

                // Корректное завершение пулов
                readPool.shutdownNow();
                writePool.shutdownNow();

            } catch (IOException e) {
                logger.warning("Ошибка закрытия ресурсов: " + e.getMessage());
            }
            logger.info("Сервер остановлен");
        }
    }

    public void stop() {
        isRunning = false;
    }

    public static void main(String[] args) {
        String dbConfig = "db.properties";

        if (args.length >= 1) {
            try {
                PORT = Integer.parseInt(args[0]);
                if (args.length >= 2) {
                    dbConfig = args[1];
                }
            } catch (NumberFormatException e) {
                dbConfig = args[0];
            }
        }

        System.out.println("Порт: " + PORT);
        System.out.println("Конфиг БД: " + dbConfig);

        new java.io.File("logs").mkdirs();

        Server server = new Server(dbConfig);
        server.start();
    }
}
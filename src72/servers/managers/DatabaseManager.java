package servers.managers;

import common.City;
import common.Coordinates;
import common.Climate;
import common.Government;
import common.StandardOfLiving;
import common.Human;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Properties;
import java.util.logging.Logger;

public class DatabaseManager {
    private static final Logger logger = Logger.getLogger(DatabaseManager.class.getName());
    private static DatabaseManager instance;
    private Connection connection;

    private static final String DEFAULT_HOST = "pg";
    private static final String DEFAULT_DB = "studs";
    private String user;
    private String password;
    private String pepper;

    private volatile boolean dbAvailable = false;
    private volatile boolean reconnecting = false;
    private String dbConfigFile;

    private DatabaseManager() {}

    public static synchronized DatabaseManager getInstance() {
        if (instance == null) {
            instance = new DatabaseManager();
        }
        return instance;
    }

    public boolean isDbAvailable() {
        return dbAvailable;
    }

    public boolean isConnected() {
        try {
            return connection != null && !connection.isClosed() && connection.isValid(2);
        } catch (SQLException e) {
            return false;
        }
    }

    public synchronized boolean reconnect() {
        if (reconnecting) {
            logger.warning("Переподключение уже выполняется");
            return false;
        }

        reconnecting = true;
        logger.info("Попытка переподключения к БД...");

        try {
            // Закрываем старое соединение если есть
            if (connection != null) {
                try {
                    if (!connection.isClosed()) {
                        connection.close();
                    }
                } catch (SQLException ignored) {}
            }

            connection = null;

            // ПЕРЕЧИТЫВАЕМ конфигурацию перед подключением!
            if (dbConfigFile != null) {
                loadConfig(dbConfigFile);
            }

            connect();

            if (isConnected()) {
                createTables();
                dbAvailable = true;
                logger.info("Переподключение к БД успешно выполнено");
                reconnecting = false;
                return true;
            } else {
                logger.severe("Не удалось установить соединение с БД");
                dbAvailable = false;
                reconnecting = false;
                return false;
            }
        } catch (Exception e) {
            logger.severe("Переподключение не удалось: " + e.getMessage());
            dbAvailable = false;
            reconnecting = false;
            return false;
        }
    }

    public boolean init(String dbConfigFile) {
        this.dbConfigFile = dbConfigFile; // Сохраняем путь для последующего reconnect
        try {
            loadConfig(dbConfigFile);
            connect();
            createTables();
            dbAvailable = true;
            logger.info("База данных инициализирована успешно.");
            return true;
        } catch (Exception e) {
            logger.severe("Ошибка инициализации БД: " + e.getMessage());
            logger.warning("Сервер будет работать в локальном режиме без подключения к БД");
            dbAvailable = false;
            return false;
        }
    }

    private void loadConfig(String dbConfigFile) {
        try (InputStream input = new FileInputStream(dbConfigFile)) {
            Properties prop = new Properties();
            prop.load(input);
            this.user = prop.getProperty("db.user", "s504836");
            this.password = prop.getProperty("db.password", "");
            this.pepper = prop.getProperty("secret.pepper", "DefaultPepperKeyIsNotCool");
            logger.info("Конфигурация БД загружена успешно. Пользователь: " + this.user);
        } catch (java.nio.file.AccessDeniedException e) {
            logger.warning("Нет прав на чтение db.properties. Используются значения по умолчанию.");
            this.user = "s504836";
            this.password = "";
            this.pepper = "DefaultPepperKeyIsNotCool";
            dbAvailable = false;
        } catch (IOException e) {
            logger.warning("Файл db.properties не найден, используются значения по умолчанию.");
            this.user = "s504836";
            this.password = "";
            this.pepper = "DefaultPepperKeyIsNotCool";
            dbAvailable = false;
        }
    }

    private void connect() throws SQLException {
        String url = String.format("jdbc:postgresql://%s:5432/%s", DEFAULT_HOST, DEFAULT_DB);
        if (connection != null && !connection.isClosed()) return;

        try {
            Class.forName("org.postgresql.Driver");
        } catch (ClassNotFoundException e) {
            logger.severe("Драйвер PostgreSQL не найден.");
            throw new SQLException("Driver missing", e);
        }

        Properties props = new Properties();
        props.setProperty("user", user);
        props.setProperty("password", password);
        props.setProperty("ssl", "false");

        logger.info("Подключение к БД: " + url + " как " + user);
        connection = DriverManager.getConnection(url, props);
        logger.info("Подключение к БД установлено");
    }

    private void createTables() {
        if (!isConnected()) {
            logger.warning("Невозможно создать таблицы: нет соединения с БД");
            return;
        }

        try (Statement stmt = connection.createStatement()) {
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS users (
                    login VARCHAR(50) PRIMARY KEY,
                    password_hash VARCHAR(100) NOT NULL,
                    salt VARCHAR(64) NOT NULL
                )
            """);

            stmt.execute("""
                CREATE TABLE IF NOT EXISTS cities (
                    id SERIAL PRIMARY KEY,
                    name TEXT NOT NULL,
                    x FLOAT CHECK (x > -872),
                    y INT CHECK (y > -846),
                    creation_date DATE DEFAULT CURRENT_DATE,
                    area INT CHECK (area > 0),
                    population BIGINT CHECK (population > 0),
                    meters DOUBLE PRECISION,
                    climate VARCHAR(20),
                    government VARCHAR(20),
                    standard_of_living VARCHAR(20),
                    governor_name VARCHAR(50),
                    governor_age INT,
                    author_login VARCHAR(50) REFERENCES users(login) ON DELETE CASCADE
                )
            """);
            logger.info("Таблицы проверены/созданы");
        } catch (SQLException e) {
            logger.severe("Ошибка создания таблиц: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public synchronized ArrayList<City> loadAllCities() {
        if (!isConnected()) {
            logger.warning("БД недоступна. Возвращаем пустой список");
            return new ArrayList<>();
        }

        ArrayList<City> list = new ArrayList<>();
        String query = "SELECT * FROM cities ORDER BY id";
        try (PreparedStatement ps = connection.prepareStatement(query);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                try {
                    City city = mapRowToCity(rs);
                    list.add(city);
                } catch (Exception e) {
                    logger.severe("Ошибка при загрузке строки: " + e.getMessage());
                    e.printStackTrace();
                }
            }
            logger.info("Всего загружено из БД: " + list.size() + " элементов");
        } catch (SQLException e) {
            logger.severe("Ошибка загрузки коллекции: " + e.getMessage());
            e.printStackTrace();
        }
        return list;
    }

    public synchronized long addCity(City city, String author) {
        if (!isConnected()) {
            logger.warning("БД недоступна. Элемент не сохранён");
            return -1;
        }

        String query = """
            INSERT INTO cities (name, x, y, creation_date, area, population, meters, climate, government, standard_of_living, governor_name, governor_age, author_login)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            RETURNING id
            """;
        try (PreparedStatement ps = connection.prepareStatement(query)) {
            fillStatement(ps, city, author);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                long id = rs.getLong("id");
                logger.info("Элемент добавлен в БД с ID: " + id);
                return id;
            }
        } catch (SQLException e) {
            logger.severe("Ошибка добавления в БД: " + e.getMessage());
            e.printStackTrace();
        }
        return -1;
    }

    public synchronized boolean updateCity(long id, City city, String author) {
        if (!isConnected()) {
            logger.warning("БД недоступна. Обновление пропущено");
            return false;
        }

        String query = """
            UPDATE cities SET  
                name=?, x=?, y=?, creation_date=?, area=?, population=?,  
                meters=?, climate=?, government=?, standard_of_living=?,  
                governor_name=?, governor_age=?
            WHERE id=? AND author_login=?
            """;
        try (PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setString(1, city.getName());
            ps.setFloat(2, city.getCoordinates().getX());
            ps.setInt(3, city.getCoordinates().getY());
            ps.setDate(4, Date.valueOf(city.getCreationDate()));
            ps.setInt(5, city.getArea());
            ps.setLong(6, city.getPopulation());
            Double meters = city.getMetersAboveSeaLevel() != null ?
                    city.getMetersAboveSeaLevel().doubleValue() : null;
            if (meters != null) {
                ps.setDouble(7, meters);
            } else {
                ps.setNull(7, Types.DOUBLE);
            }
            ps.setString(8, city.getClimate().name());
            ps.setString(9, city.getGovernment().name());
            ps.setString(10, city.getStandardOfLiving().name());
            if (city.getGovernor() != null) {
                ps.setString(11, city.getGovernor().getName());
                ps.setInt(12, city.getGovernor().getAge());
            } else {
                ps.setNull(11, Types.VARCHAR);
                ps.setNull(12, Types.INTEGER);
            }
            ps.setLong(13, id);
            ps.setString(14, author);
            int rows = ps.executeUpdate();
            logger.info("Обновлено строк: " + rows);
            return rows > 0;
        } catch (SQLException e) {
            logger.severe("Ошибка обновления в БД: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public synchronized boolean deleteCity(long id, String author) {
        if (!isConnected()) {
            logger.warning("БД недоступна. Удаление пропущено");
            return false;
        }

        String query = "DELETE FROM cities WHERE id = ? AND author_login = ?";
        try (PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setLong(1, id);
            ps.setString(2, author);
            int rows = ps.executeUpdate();
            logger.info("Удалено строк: " + rows);
            return rows > 0;
        } catch (SQLException e) {
            logger.severe("Ошибка удаления из БД: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public synchronized int clearDB(String author) {
        if (!isConnected()) {
            logger.warning("БД недоступна. Очистка пропущена");
            return 0;
        }

        if (author == null || author.trim().isEmpty()) {
            return 0;
        }
        String query = "DELETE FROM cities WHERE author_login = ?";
        try (PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setString(1, author);
            int rowsDeleted = ps.executeUpdate();
            logger.info("Очистка коллекции: удалено " + rowsDeleted + " элементов для автора '" + author + "'");
            return rowsDeleted;
        } catch (SQLException e) {
            logger.severe("Ошибка очистки БД для автора " + author + ": " + e.getMessage());
            e.printStackTrace();
            return -1;
        }
    }

    public synchronized boolean checkUserPassword(String login, String rawPassword) {
        if (!isConnected()) {
            logger.warning("БД недоступна. Авторизация невозможна");
            return false;
        }

        String query = "SELECT salt, password_hash FROM users WHERE login=?";
        try (PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setString(1, login);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                String salt = rs.getString("salt");
                String storedHash = rs.getString("password_hash");
                String inputHash = hashPassword(rawPassword, salt);
                boolean match = storedHash.equals(inputHash);
                logger.info("Проверка пароля для '" + login + "': " + (match ? "OK" : "FAIL"));
                return match;
            }
            logger.info("Пользователь '" + login + "' не найден");
            return false;
        } catch (SQLException e) {
            logger.severe("Ошибка проверки пароля: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public synchronized boolean register(String login, String rawPassword) {
        if (!isConnected()) {
            logger.warning("БД недоступна. Регистрация невозможна");
            return false;
        }

        String query = "INSERT INTO users (login, password_hash, salt) VALUES (?, ?, ?)";
        try {
            String salt = generateSalt();
            String hash = hashPassword(rawPassword, salt);
            try (PreparedStatement ps = connection.prepareStatement(query)) {
                ps.setString(1, login);
                ps.setString(2, hash);
                ps.setString(3, salt);
                ps.executeUpdate();
            }
            logger.info("Пользователь зарегистрирован: " + login);
            return true;
        } catch (SQLException e) {
            if (e.getSQLState().equals("23505")) {
                logger.info("Пользователь уже существует: " + login);
                return false;
            }
            logger.severe("Ошибка регистрации: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    private City mapRowToCity(ResultSet rs) throws SQLException {
        City city = new City();
        city.setId(rs.getLong("id"));
        city.setName(rs.getString("name"));
        float x = rs.getFloat("x");
        int y = rs.getInt("y");
        city.setCoordinates(new Coordinates(x, y));
        Date sqlDate = rs.getDate("creation_date");
        city.setCreationDate(sqlDate != null ? sqlDate.toLocalDate() : LocalDate.now());
        city.setArea(rs.getInt("area"));
        city.setPopulation(rs.getLong("population"));
        double meters = rs.getDouble("meters");
        if (rs.wasNull()) city.setMetersAboveSeaLevel(null); else city.setMetersAboveSeaLevel((float) meters);
        try { city.setClimate(Climate.valueOf(rs.getString("climate"))); } catch (Exception e) { city.setClimate(Climate.MEDITERRANIAN); }
        try { city.setGovernment(Government.valueOf(rs.getString("government"))); } catch (Exception e) { city.setGovernment(Government.NOOCRACY); }
        try { city.setStandardOfLiving(StandardOfLiving.valueOf(rs.getString("standard_of_living"))); } catch (Exception e) { city.setStandardOfLiving(StandardOfLiving.MEDIUM); }
        String govName = rs.getString("governor_name");
        int govAge = rs.getInt("governor_age");
        if (govName != null && !rs.wasNull()) city.setGovernor(new Human(govName, govAge));
        city.setAuthorLogin(rs.getString("author_login"));
        return city;
    }

    private void fillStatement(PreparedStatement ps, City city, String author) throws SQLException {
        ps.setString(1, city.getName());
        ps.setFloat(2, city.getCoordinates().getX());
        ps.setInt(3, city.getCoordinates().getY());
        ps.setDate(4, Date.valueOf(city.getCreationDate()));
        ps.setInt(5, city.getArea());
        ps.setLong(6, city.getPopulation());
        Double meters = city.getMetersAboveSeaLevel() != null ?
                city.getMetersAboveSeaLevel().doubleValue() : null;
        if (meters != null) {
            ps.setDouble(7, meters);
        } else {
            ps.setNull(7, Types.DOUBLE);
        }
        ps.setString(8, city.getClimate().name());
        ps.setString(9, city.getGovernment().name());
        ps.setString(10, city.getStandardOfLiving().name());
        if (city.getGovernor() != null) {
            ps.setString(11, city.getGovernor().getName());
            ps.setInt(12, city.getGovernor().getAge());
        } else {
            ps.setNull(11, Types.VARCHAR);
            ps.setNull(12, Types.INTEGER);
        }
        ps.setString(13, author);
    }

    private String generateSalt() {
        byte[] salt = new byte[16];
        new SecureRandom().nextBytes(salt);
        return Base64.getEncoder().encodeToString(salt);
    }

    private String hashPassword(String password, String salt) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            String combined = password + salt + pepper;
            byte[] digest = md.digest(combined.getBytes());
            return Base64.getEncoder().encodeToString(digest);
        } catch (NoSuchAlgorithmException e) {
            logger.severe("Алгоритм хеширования не найден: " + e.getMessage());
            return null;
        }
    }

    public synchronized void syncCollectionToDb(ArrayList<City> cities) {
        if (!isConnected()) {
            logger.warning("БД недоступна. Синхронизация пропущена");
            return;
        }

        if (cities == null || cities.isEmpty()) {
            logger.info("Коллекция пуста, синхронизация не требуется.");
            return;
        }

        String sql = """
            INSERT INTO cities (id, name, x, y, creation_date, area, population, meters, climate, government, standard_of_living, governor_name, governor_age, author_login)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            ON CONFLICT (id) DO UPDATE SET
                name = EXCLUDED.name, x = EXCLUDED.x, y = EXCLUDED.y, creation_date = EXCLUDED.creation_date,
                area = EXCLUDED.area, population = EXCLUDED.population, meters = EXCLUDED.meters,
                climate = EXCLUDED.climate, government = EXCLUDED.government, standard_of_living = EXCLUDED.standard_of_living,
                governor_name = EXCLUDED.governor_name, governor_age = EXCLUDED.governor_age, author_login = EXCLUDED.author_login
            """;

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            for (City city : cities) {
                ps.setLong(1, city.getId());
                ps.setString(2, city.getName());
                ps.setFloat(3, city.getCoordinates().getX());
                ps.setInt(4, city.getCoordinates().getY());
                ps.setDate(5, Date.valueOf(city.getCreationDate()));
                ps.setInt(6, city.getArea());
                ps.setLong(7, city.getPopulation());
                Double meters = city.getMetersAboveSeaLevel() != null ?
                        city.getMetersAboveSeaLevel().doubleValue() : null;
                if (meters != null) {
                    ps.setDouble(8, meters);
                } else {
                    ps.setNull(8, Types.DOUBLE);
                }
                ps.setString(9, city.getClimate().name());
                ps.setString(10, city.getGovernment().name());
                ps.setString(11, city.getStandardOfLiving().name());
                if (city.getGovernor() != null) {
                    ps.setString(12, city.getGovernor().getName());
                    ps.setInt(13, city.getGovernor().getAge());
                } else {
                    ps.setNull(12, Types.VARCHAR);
                    ps.setNull(13, Types.INTEGER);
                }
                ps.setString(14, city.getAuthorLogin());
                ps.addBatch();
            }
            int[] results = ps.executeBatch();
            logger.info("Синхронизировано с БД: " + results.length + " элементов");
        } catch (SQLException e) {
            logger.severe("Ошибка синхронизации коллекции с БД: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
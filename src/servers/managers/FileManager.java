package servers.managers;

import common.City;
import com.google.gson.*;
import com.google.gson.reflect.TypeToken;

import java.io.*;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.logging.Logger;

/**
 * Менеджер для работы с JSON-файлами.
 * Использует Gson для сериализации/десериализации.
 *
 * @author Максим
 */
public class FileManager {
    private static final Logger logger = Logger.getLogger(FileManager.class.getName());
    private static final DateTimeFormatter DATE_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd");

    /**
     * Gson-инстанс с настройками для LocalDate.
     */
    private static final Gson GSON = new GsonBuilder()
            .registerTypeAdapter(LocalDate.class, (JsonSerializer<LocalDate>)
                    (date, type, ctx) -> new JsonPrimitive(date.format(DATE_FORMATTER)))
            .registerTypeAdapter(LocalDate.class, (JsonDeserializer<LocalDate>)
                    (json, type, ctx) -> LocalDate.parse(json.getAsString(), DATE_FORMATTER))
            .setPrettyPrinting()
            .serializeNulls()
            .create();

    /**
     * Читает коллекцию из JSON-файла.
     *
     * @param filePath путь к файлу
     * @param clazz класс типа элементов
     * @return коллекция объектов или пустой список при ошибке
     */
    public ArrayList<City> readFromJsonFile(String filePath, Class<City> clazz) {
        logger.info("Чтение коллекции из файла: " + filePath);

        try {
            StringBuilder jsonContent = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(new FileInputStream(filePath), StandardCharsets.UTF_8))) {

                String line;
                while ((line = reader.readLine()) != null) {
                    jsonContent.append(line);
                }
            }

            Type listType = TypeToken.getParameterized(ArrayList.class, clazz).getType();
            ArrayList<City> collection = GSON.fromJson(jsonContent.toString(), listType);

            logger.info("Загружено элементов: " + (collection != null ? collection.size() : 0));
            return collection != null ? collection : new ArrayList<>();

        } catch (IOException e) {
            logger.warning("Не удалось прочитать файл: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    /**
     * Записывает коллекцию в JSON-файл.
     *
     * @param filePath путь к файлу
     * @param list коллекция объектов
     * @throws IOException если произошла ошибка записи
     */
    public void writeToJsonFile(String filePath, ArrayList<City> list) throws IOException {
        logger.info("Сохранение коллекции в файл: " + filePath);

        // Создаём директорию, если не существует
        File file = new File(filePath);
        File parentDir = file.getParentFile();
        if (parentDir != null && !parentDir.exists()) {
            parentDir.mkdirs();
        }

        String json = GSON.toJson(list);
        byte[] bytes = json.getBytes(StandardCharsets.UTF_8);

        try (BufferedOutputStream bos = new BufferedOutputStream(
                new FileOutputStream(filePath))) {
            bos.write(bytes);
            bos.flush();
        }

        logger.info("Коллекция сохранена: " + list.size() + " элементов");
    }
}
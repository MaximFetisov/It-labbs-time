package main;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import java.io.*;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

/**
 * Менеджер для работы с JSON-файлами с использованием библиотеки Gson.
 * Обеспечивает сериализацию и десериализацию коллекций объектов, включая
 * поддержку типов дат и времени через кастомные адаптеры.
 * <p>
 * Использует {@link BufferedReader} для чтения и {@link BufferedOutputStream}
 * для записи в соответствии с требованиями задания.
 * </p>
 *
 * @author Максим
 * @see Gson
 * @see Run#collectionManager
 */
public class FileParser {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter DATETIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm:ss");

    /**
     * Адаптер для сериализации/десериализации {@link LocalDate}.
     */
    private static class LocalDateAdapter extends TypeAdapter<LocalDate> {
        @Override
        public void write(JsonWriter out, LocalDate value) throws IOException {
            out.value(value == null ? null : value.format(DATE_FORMATTER));
        }

        @Override
        public LocalDate read(JsonReader in) throws IOException {
            String value = in.nextString();
            return value == null ? null : LocalDate.parse(value, DATE_FORMATTER);
        }
    }

    /**
     * Адаптер для сериализации/десериализации {@link LocalDateTime}.
     */
    private static class LocalDateTimeAdapter extends TypeAdapter<LocalDateTime> {
        @Override
        public void write(JsonWriter out, LocalDateTime value) throws IOException {
            out.value(value == null ? null : value.format(DATETIME_FORMATTER));
        }

        @Override
        public LocalDateTime read(JsonReader in) throws IOException {
            String value = in.nextString();
            return value == null ? null : LocalDateTime.parse(value, DATETIME_FORMATTER);
        }
    }

    /**
     * Адаптер для сериализации/десериализации {@link LocalTime}.
     */
    private static class LocalTimeAdapter extends TypeAdapter<LocalTime> {
        @Override
        public void write(JsonWriter out, LocalTime value) throws IOException {
            out.value(value == null ? null : value.format(TIME_FORMATTER));
        }

        @Override
        public LocalTime read(JsonReader in) throws IOException {
            String value = in.nextString();
            return value == null ? null : LocalTime.parse(value, TIME_FORMATTER);
        }
    }

    /**
     * Адаптер для сериализации/десериализации {@link ZonedDateTime}.
     */
    private static class ZonedDateTimeAdapter extends TypeAdapter<ZonedDateTime> {
        @Override
        public void write(JsonWriter out, ZonedDateTime value) throws IOException {
            out.value(value == null ? null : value.format(DateTimeFormatter.ISO_ZONED_DATE_TIME));
        }

        @Override
        public ZonedDateTime read(JsonReader in) throws IOException {
            String value = in.nextString();
            return value == null ? null : ZonedDateTime.parse(value, DateTimeFormatter.ISO_ZONED_DATE_TIME);
        }
    }

    /**
     * Адаптер для сериализации/десериализации {@link Instant}.
     */
    private static class InstantAdapter extends TypeAdapter<Instant> {
        @Override
        public void write(JsonWriter out, Instant value) throws IOException {
            out.value(value == null ? null : value.toString());
        }

        @Override
        public Instant read(JsonReader in) throws IOException {
            String value = in.nextString();
            return value == null ? null : Instant.parse(value);
        }
    }

    /**
     * Экземпляр Gson с зарегистрированными адаптерами для типов времени.
     */
    private static final Gson GSON = new GsonBuilder()
            .registerTypeAdapter(LocalDate.class, new LocalDateAdapter())
            .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())
            .registerTypeAdapter(LocalTime.class, new LocalTimeAdapter())
            .registerTypeAdapter(ZonedDateTime.class, new ZonedDateTimeAdapter())
            .registerTypeAdapter(Instant.class, new InstantAdapter())
            .setPrettyPrinting()
            .serializeNulls()
            .create();

    /**
     * Записывает коллекцию объектов в JSON-файл.
     * <p>
     * Использует {@link BufferedOutputStream} для записи.
     * </p>
     *
     * @param <T> тип элементов коллекции
     * @param filePath путь к файлу для записи
     * @param list коллекция объектов для сериализации
     * @throws IOException если произошла ошибка ввода-вывода
     */
    public static <T> void writeToJsonFile(String filePath, ArrayList<T> list) throws IOException {
        String json = GSON.toJson(list);
        byte[] bytes = json.getBytes(StandardCharsets.UTF_8);

        try (BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(filePath))) {
            bos.write(bytes);
        }
    }

    /**
     * Читает коллекцию объектов из JSON-файла.
     * <p>
     * Использует {@link BufferedReader} для чтения.
     * </p>
     *
     * @param <T> тип элементов коллекции
     * @param filePath путь к файлу для чтения
     * @param clazz класс типа элементов коллекции
     * @return коллекция объектов, десериализованная из JSON
     * @throws IOException если произошла ошибка ввода-вывода
     */
    public static <T> ArrayList<T> readFromJsonFile(String filePath, Class<T> clazz) throws IOException {
        StringBuilder jsonContent = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(new FileInputStream(filePath), StandardCharsets.UTF_8))) {

            String line;
            while ((line = reader.readLine()) != null) {
                jsonContent.append(line);
            }
        }
        Type listType = TypeToken.getParameterized(ArrayList.class, clazz).getType();
        ArrayList<T> collection = GSON.fromJson(jsonContent.toString(), listType);

        return collection;
    }

    /**
     * Возвращает экземпляр Gson для использования в других классах.
     *
     * @return настроенный экземпляр Gson
     */
    public static Gson getGson() {
        return GSON;
    }
}
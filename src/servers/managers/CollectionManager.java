package servers.managers;

import common.City;
import common.Human;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Deque;
import java.util.LinkedList;
import java.util.stream.Collectors;

/**
 * Менеджер коллекции, отвечающий за хранение и управление элементами City.
 * Все операции реализованы через Stream API (требование задания).
 *
 * @author Максим
 * @see City
 */
public class CollectionManager {
    private ArrayList<City> cities;
    private long nextId;
    private LocalDateTime initializationDate;
    private Deque<String> commandHistory;
    private static final int MAX_HISTORY_SIZE = 9;
    /**
     * Создаёт новый менеджер коллекции.
     */
    public CollectionManager() {
        this.cities = new ArrayList<>();
        this.nextId = 1L;
        this.commandHistory = new LinkedList<>();
        this.initializationDate = LocalDateTime.now();
    }

    /**
     * Добавляет город в коллекцию.
     */
    public void add(City city) {
        cities.add(city);
    }

    /**
     * Возвращает коллекцию городов (без сортировки).
     * Используется для внутренних операций (сохранение, обработка).
     */
    public ArrayList<City> getCities() {
        return new ArrayList<>(cities);
    }
    /**
     * Возвращает отсортированный список городов.
     * Используется ТОЛЬКО для команд, где нужна сортировка (min_by_coordinates и т.д.)
     */
    public ArrayList<City> getSortedCities() {
        return cities.stream()
                .sorted()
                .collect(Collectors.toCollection(ArrayList::new));
    }

    /**
     * Возвращает размер коллекции через Stream API.
     */
    public int getSize() {
        return (int) cities.stream().count();
    }

    /**
     * Очищает коллекцию.
     */
    public void clear() {
        cities.clear();
    }

    /**
     * Находит город по ID через Stream API.
     */
    public City getById(long id) {
        return cities.stream()
                .filter(city -> city.getId() == id)
                .findFirst()
                .orElse(null);
    }

    /**
     * Удаляет город по ID через Stream API.
     */
    public boolean removeById(long id) {
        return cities.removeIf(city -> city.getId() == id);
    }

    /**
     * Обновляет город по ID.
     *
     * @param id идентификатор
     * @param newCity новый объект
     * @return true если обновлён
     */
    public boolean update(long id, City newCity) {
        // ✅ Через Stream API + AtomicInteger для возврата результата
        java.util.concurrent.atomic.AtomicBoolean updated = new java.util.concurrent.atomic.AtomicBoolean(false);

        cities.stream()
                .filter(city -> city.getId() == id)
                .findFirst()
                .ifPresent(city -> {
                    int index = cities.indexOf(city);
                    if (index >= 0) {
                        cities.set(index, newCity);
                        updated.set(true);
                    }
                });

        return updated.get();
    }

    /**
     * Возвращает сумму metersAboveSeaLevel через Stream API.
     */
    public double getSumOfMeters() {
        return cities.stream()
                .mapToDouble(city -> city.getMetersAboveSeaLevel() != null ?
                        city.getMetersAboveSeaLevel() : 0.0)
                .sum();
    }

    /**
     * Возвращает город с минимальными координатами через Stream API.
     */
    public City getMinByCoordinates() {
        return cities.stream()
                .min(Comparator.comparing(City::getCoordinates))
                .orElse(null);
    }

    /**
     * Возвращает уникальных губернаторов через Stream API.
     */
    public ArrayList<Human> getUniqueGovernors() {
        return cities.stream()
                .map(City::getGovernor)
                .filter(gov -> gov != null)
                .distinct()
                .collect(Collectors.toCollection(ArrayList::new));
    }

    /**
     * Удаляет элементы меньшие заданного через Stream API.
     */
    public int removeLower(City referenceCity) {
        int initialSize = cities.size();
        cities.removeIf(city -> city.compareTo(referenceCity) < 0);
        return initialSize - cities.size();
    }

    /**
     * Удаляет первый элемент.
     */
    public void removeFirst() {
        cities.stream()
                .findFirst()
                .ifPresent(city -> cities.remove(city));
    }
    /**
     * Устанавливает следующий ID.
     */
    public void setNextId(long nextId) {
        this.nextId = nextId;
    }

    /**
     * Генерирует следующий ID.
     */
    public long generateId() {
        return nextId++;
    }

    /**
     * Устанавливает коллекцию из файла.
     */
    public void setCities(ArrayList<City> cities) {
        this.cities = cities;
        if (!cities.isEmpty()) {
            this.nextId = cities.stream()
                    .mapToLong(City::getId)
                    .max()
                    .orElse(0L) + 1;
        }
    }
    /**
     * Добавляет команду в историю.
     *
     * @param commandName имя команды
     */
    public void addToHistory(String commandName) {
        if (commandHistory.size() >= MAX_HISTORY_SIZE) {
            commandHistory.removeFirst();  // Удаляем самую старую команду
        }
        commandHistory.addLast(commandName);
    }

    /**
     * Возвращает историю команд.
     *
     * @return Deque с именами команд
     */
    public Deque<String> getCommandHistory() {
        return new LinkedList<>(commandHistory);  // Возвращаем копию
    }

    /**
     * Очищает историю команд.
     */
    public void clearHistory() {
        commandHistory.clear();
    }

    /**
     * Возвращает дату инициализации коллекции.
     *
     * @return дата и время создания коллекции
     */
    public LocalDateTime getInitializationDate() {
        return initializationDate;
    }
}
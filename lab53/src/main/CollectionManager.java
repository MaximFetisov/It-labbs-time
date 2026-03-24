package main;

import datass.City;
import java.util.ArrayList;
import java.time.LocalDateTime;

/**
 * Менеджер коллекции, отвечающий за хранение и управление элементами City.
 * Использует ArrayList для хранения элементов и хранит дату инициализации коллекции.
 *
 * @author Максим
 * @see City
 */
public class CollectionManager {
    private ArrayList<City> cities;
    private LocalDateTime initializationDate;

    /**
     * Создаёт новый менеджер коллекции с пустым списком городов.
     * Инициализирует дату создания коллекции текущим моментом времени.
     */
    public CollectionManager() {
        cities = new ArrayList<>();
        initializationDate = LocalDateTime.now();
    }

    /**
     * Добавляет новый город в коллекцию.
     *
     * @param city объект City для добавления
     */
    public void add(City city) {
        cities.add(city);
    }

    /**
     * Возвращает список всех городов в коллекции.
     *
     * @return ArrayList с объектами City
     */
    public ArrayList<City> getCities() {
        return cities;
    }

    /**
     * Возвращает количество элементов в коллекции.
     *
     * @return размер коллекции
     */
    public int getSize() {
        return cities.size();
    }

    /**
     * Возвращает дату инициализации коллекции.
     *
     * @return дата и время создания коллекции
     */
    public LocalDateTime getInitializationDate() {
        return initializationDate;
    }

    /**
     * Очищает коллекцию, удаляя все элементы.
     */
    public void clear() {
        cities.clear();
    }

    /**
     * Находит город по его идентификатору.
     *
     * @param id идентификатор города для поиска
     * @return объект City или null, если не найден
     */
    public City getById(long id) {
        for (City city : cities) {
            if (city.getId() == id) {
                return city;
            }
        }
        return null;
    }

    /**
     * Удаляет город из коллекции по его идентификатору.
     *
     * @param id идентификатор города для удаления
     * @return true, если элемент найден и удалён, иначе false
     */
    public boolean removeById(long id) {
        return cities.removeIf(city -> city.getId() == id);
    }

    /**
     * Устанавливает новый список городов в коллекцию.
     *
     * @param cities новый ArrayList с объектами City
     */
    public void setCities(ArrayList<City> cities) {
        this.cities = cities;
    }
}
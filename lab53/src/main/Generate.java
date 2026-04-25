package main;

/**
 * Генератор уникальных идентификаторов типа {@code long} для элементов коллекции.
 * Предоставляет статические методы для получения последовательных ID, сброса и ручной установки счётчика.
 *
 * @author Максим
 * @see Run#collectionManager
 */
public class Generate {
    private static long nextId = 1L;

    /**
     * Генерирует и возвращает следующий уникальный идентификатор.
     *
     * @return текущее значение ID с последующим увеличением счётчика
     */
    public static long generateId() {
        return nextId++;
    }

    /**
     * Устанавливает следующее значение идентификатора вручную.
     *
     * @param nexId новое значение для счётчика ID
     */
    public static void setId(long nexId) {
        nextId = nexId;
    }
}
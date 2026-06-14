package common;

import java.io.Serializable;

/**
 * Класс, представляющий координаты города.
 * Содержит координаты X и Y с ограничениями на допустимые значения.
 * <p>
 * Реализует интерфейсы {@link Comparable} для сравнения координат
 * и {@link Serializable} для передачи по сети.
 * </p>
 *
 * @author Максим
 */
public class Coordinates implements Comparable<Coordinates>, Serializable {
    private static final long serialVersionUID = 1L;

    private float x;
    private int y;

    /**
     * Создаёт объект координат с заданными значениями.
     *
     * @param x координата X (float, > -872)
     * @param y координата Y (int, > -846)
     */
    public Coordinates(float x, int y) {
        this.x = x;
        this.y = y;
    }
    public float getX() { return x; }
    public int getY() { return y; }

    public void setX(float x) { this.x = x; }
    public void setY(int y) { this.y = y; }

    /**
     * Сравнивает координаты сначала по X, затем по Y.
     *
     * @param other другие координаты для сравнения
     * @return отрицательное, нулевое или положительное значение
     */
    @Override
    public int compareTo(Coordinates other) {
        int xCompare = Float.compare(this.x, other.x);
        if (xCompare != 0) return xCompare;
        return Integer.compare(this.y, other.y);
    }

    /**
     * Возвращает строковое представление координат.
     *
     * @return строка формата "X, Y"
     */
    @Override
    public String toString() {
        return x + ", " + y;
    }
}
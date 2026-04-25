package datass;

/**
 * Класс, представляющий координаты города.
 * Содержит координаты X и Y с ограничениями на допустимые значения.
 * Реализует интерфейс {@link Comparable} для сравнения координат.
 */
public class Coordinates implements Comparable<Coordinates> {
    private float x; // Значение поля должно быть больше -872
    private int y;   // Значение поля должно быть больше -846



    public Coordinates(float x, int y) {
        this.x = x;
        this.y = y;
    }


    public float getX() { return x; }
    public int getY() { return y; }

    @Override
    public int compareTo(Coordinates other) {
        int xCompare = Float.compare(this.x, other.x);
        if (xCompare != 0) return xCompare;
        return Integer.compare(this.y, other.y);
    }
}
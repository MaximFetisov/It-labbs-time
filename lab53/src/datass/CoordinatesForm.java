package datass;

import main.ChackValues;
import main.Run;

/**
 * Класс-билдер для создания объектов {@link Coordinates}.
 * Запрашивает и валидирует координаты X и Y через пользовательский ввод.
 *
 * @author Максим
 * @see Coordinates
 * @see Run#inout
 */
public class CoordinatesForm {

    /**
     * Создаёт и возвращает новый объект Coordinates с введёнными пользователем данными.
     *
     * @return новый объект Coordinates
     */
    public Coordinates build() {
        Coordinates coordinates = new Coordinates(setX(), setY());
        return coordinates;
    }

    /**
     * Запрашивает и возвращает координату X.
     *
     * @return значение X (float, больше -872)
     */
    private Float setX() {
    Run.inout.write("Введите значение X:");

    while (true) {
        String testX = ChackValues.chackValuesNull("X");
        try {
            float x = Float.parseFloat(testX.replace(",", "."));
            if (x <= -872) {
                Run.inout.write("X должно быть больше -872");
            } else if (x > 100000) {
                Run.inout.write("X должно быть не больше 100000");
            }else {
                return x;
            }
        } catch (NumberFormatException e) {
            Run.inout.write("X должно быть типа float");
        }
    }
}

    /**
     * Запрашивает и возвращает координату Y.
     *
     * @return значение Y (int, больше -846)
     */
    private Integer setY() {
        Run.inout.write("Введите значение Y:");
        while (true) {
            try {
                String testY = ChackValues.chackValuesNull("Y");
                int y = Integer.parseInt(testY);
                if (y <= -846) {
                    Run.inout.write("Y должно быть больше -846");
                } else {
                    return y;
                }
            } catch (NumberFormatException e) {
                Run.inout.write("Y должно быть типа int");
            }
        }
    }
}
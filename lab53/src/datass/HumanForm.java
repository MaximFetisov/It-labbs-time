package datass;

import main.ChackValues;
import main.Run;

/**
 * Класс-билдер для создания объектов {@link Human}.
 * Запрашивает и валидирует имя и возраст губернатора через пользовательский ввод.
 *
 * @author Максим
 * @see Human
 * @see Run#inout
 */
public class HumanForm {

    /**
     * Создаёт и возвращает новый объект Human с введёнными пользователем данными.
     *
     * @return новый объект Human
     */
    public Human build() {
        Human governor = new Human(setName(), setAge());
        return governor;
    }

    /**
     * Запрашивает и возвращает имя губернатора.
     *
     * @return имя губернатора (не пустая строка)
     */
    private String setName() {
        Run.inout.write("Введите имя губернатора:");
        String name = ChackValues.chackValuesNull("имя губернатора");
        return name;
    }

    /**
     * Запрашивает и возвращает возраст губернатора.
     *
     * @return возраст губернатора (положительное целое число)
     */
    private int setAge() {
        Run.inout.write("Введите возраст губернатора:");
        while (true) {
            try {
                String testAge = ChackValues.chackValuesNull("возраст губернатора");
                int age = Integer.parseInt(testAge);
                if (age <= 0) {
                    Run.inout.write("Возраст должен быть больше 0");
                } else {
                    return age;
                }
            } catch (NumberFormatException e) {
                Run.inout.write("Возраст должен быть типа Integer");
            }
        }
    }
}
package datass;

import main.ChackValues;
import main.Run;

/**
 * Класс, представляющий человека (губернатора города).
 * Содержит имя и возраст с ограничениями на допустимые значения.
 * Реализует интерфейс {@link Comparable} для сравнения людей.
 */
public class Human implements Comparable<Human> {
    private String name; // Поле не может быть null, строка не может быть пустой
    private Integer age; // Значение поля должно быть больше 0


    public Human(String name, Integer age) {
        this.name = name;
        this.age = age;
    }

    private void setName() {
        Run.inout.write("Введите имя губернатора:");
        name = ChackValues.chackValuesNull("имя губернатора");
    }

    private void setAge() {
        Run.inout.write("Введите возраст губернатора:");
        while (true) {
            try {
                String testAge = ChackValues.chackValuesNull("возраст губернатора");
                int age = Integer.parseInt(testAge);
                if (age <= 0) {
                    Run.inout.write("Возраст должен быть больше 0");
                } else {
                this.age = age;
                break;
                }
            } catch (NumberFormatException e) {
                Run.inout.write("Возраст должен быть типа Integer");
            }
        }
    }

    public String getName() { return name; }
    public Integer getAge() { return age; }

    @Override
    public String toString() {
        return "Human: имя: " + name + ", возраст: " + age;
    }

    @Override
    public int compareTo(Human other) {
        int nameCompare = Integer.compare(this.name.length(), other.name.length());
        if (nameCompare != 0) return nameCompare;
        return Integer.compare(this.age, other.age);
    }
}
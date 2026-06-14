package common;

import java.io.Serializable;

/**
 * Класс, представляющий человека (губернатора города).
 * Содержит имя и возраст с ограничениями на допустимые значения.
 * <p>
 * Реализует интерфейсы {@link Comparable} для сравнения людей
 * и {@link Serializable} для передачи по сети.
 * </p>
 *
 * @author Максим
 */
public class Human implements Comparable<Human>, Serializable {
    private static final long serialVersionUID = 1L;

    private String name;
    private Integer age;
    /**
     * Создаёт объект человека с заданными именем и возрастом.
     *
     * @param name имя человека
     * @param age возраст человека (> 0)
     */
    public Human(String name, Integer age) {
        this.name = name;
        this.age = age;
    }


    public String getName() { return name; }
    public Integer getAge() { return age; }

    public void setName(String name) { this.name = name; }
    public void setAge(Integer age) { this.age = age; }

    /**
     * Сравнивает людей по длине имени, затем по возрасту.
     *
     * @param other другой человек для сравнения
     * @return отрицательное, нулевое или положительное значение
     */
    @Override
    public int compareTo(Human other) {
        int nameCompare = Integer.compare(this.name.length(), other.name.length());
        if (nameCompare != 0) return nameCompare;
        return Integer.compare(this.age, other.age);
    }

    /**
     * Возвращает строковое представление человека.
     *
     * @return строка формата "Human: имя: ..., возраст: ..."
     */
    @Override
    public String toString() {
        return "Human: имя: " + name + ", возраст: " + age;
    }
}
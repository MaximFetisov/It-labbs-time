package datass;

import java.time.LocalDate;

import main.ChackValues;
import main.Generate;
import main.Run;

/**
 * Класс-билдер для создания объектов {@link City}.
 * Собирает все необходимые поля через пользовательский ввод и создаёт новый объект City.
 *
 * @author Максим
 * @see City
 * @see Run#coordinatesForm
 * @see Run#humanForm
 */
public class CityForm {

    /**
     * Создаёт и возвращает новый объект City с введёнными пользователем данными.
     *
     * @return новый объект City с автоматически сгенерированными id и creationDate
     */
    public City build() {
        City city = new City(
                Generate.generateId(),
                setName(),
                Run.coordinatesForm.build(),
                LocalDate.now(),
                setArea(),
                setPopulation(),
                setMetersAboveSeaLevel(),
                Climate.fromInput("климат"),
                Government.fromInput("правительство"),
                StandardOfLiving.fromInput("уровень жизни"),
                setGovernor()
        );
        return city;
    }

    /**
     * Запрашивает и возвращает название города.
     *
     * @return название города (не пустая строка)
     */
    private String setName() {
        Run.inout.write("Введите название города:");
        String name = ChackValues.chackValuesNull("Название города");
        return name;
    }

    /**
     * Запрашивает и возвращает площадь города.
     *
     * @return площадь города (положительное целое число)
     */
    private int setArea() {
        Run.inout.write("Введите площадь города:");
        while (true) {
            try {
                String test = ChackValues.chackValuesNull("площадь города");
                int area = Integer.parseInt(test);
                while (area <= 0) {
                    Run.inout.write("Площадь должна быть больше 0");
                    Run.inout.write("Введите площадь города:");
                    area = Integer.parseInt(Run.inout.read());
                }
                return area;
            } catch (NumberFormatException e) {
                Run.inout.write("Площадь должна быть типа Integer");
            }
        }
    }

    /**
     * Запрашивает и возвращает население города.
     *
     * @return население города (положительное long число)
     */
    private long setPopulation() {
        Run.inout.write("Введите население города:");
        while (true) {
            try {
                String test = ChackValues.chackValuesNull("население города");
                long population = Long.parseLong(test);
                while (population <= 0) {
                    Run.inout.write("Население должно быть больше 0");
                    Run.inout.write("Введите население города:");
                    population = Long.parseLong(Run.inout.read());
                }
                return population;
            } catch (NumberFormatException e) {
                Run.inout.write("Население должно быть типа long");
            }
        }
    }

    /**
     * Запрашивает и возвращает высоту над уровнем моря.
     *
     * @return высота над уровнем моря или null, если введена пустая строка
     */
    private Float setMetersAboveSeaLevel() {
        Float metersAboveSeaLevel;
        Run.inout.write("Введите высоту над уровнем моря (или пустую строку для null):");
        String test = Run.inout.read();
        if (test.trim().isEmpty()) {
            metersAboveSeaLevel = null;
        } else {
            try {
                metersAboveSeaLevel = Float.parseFloat(test);
            } catch (NumberFormatException e) {
                Run.inout.write("Неверный формат числа, установлено null");
                metersAboveSeaLevel = null;
            }
        }
        return metersAboveSeaLevel;
    }

    /**
     * Запрашивает и возвращает данные губернатора.
     *
     * @return объект Human или null, если введена пустая строка
     */
    private Human setGovernor() {
        Human governor;
        Run.inout.write("Введите данные губернатора (или пустую строку для null):");
        String test = Run.inout.read();
        if (test.trim().isEmpty()) {
            governor = null;
        } else {
            governor = Run.humanForm.build();
        }
        return governor;
    }
}
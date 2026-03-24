package datass;

import main.ChackValues;
import main.Generate;
import main.Run;
import java.time.LocalDate;

/**
 * Класс, представляющий город.
 * Содержит полную информацию о городе: идентификатор, название,
 * координаты, дату создания, площадь, население, высоту над уровнем моря,
 * климат, форму правления, уровень жизни и губернатора.
 * Реализует интерфейс {@link Comparable} для сравнения городов.
 *
 * @author Максим
 * @see Coordinates
 * @see Climate
 * @see Government
 * @see StandardOfLiving
 * @see Human
 */
public class City implements Comparable<City> {
    private long id; // Значение поля должно быть больше 0, уникальное, генерируется автоматически
    private String name; // Поле не может быть null, строка не может быть пустой
    private Coordinates coordinates; // Поле не может быть null
    private LocalDate creationDate; // Поле не может быть null, генерируется автоматически
    private Integer area; // Значение поля должно быть больше 0, поле не может быть null
    private long population; // Значение поля должно быть больше 0
    private Float metersAboveSeaLevel; // Может быть null
    private Climate climate; // Поле не может быть null
    private Government government; // Поле не может быть null
    private StandardOfLiving standardOfLiving; // Поле не может быть null
    private Human governor; // Может быть null

    

    public City(long id) {
        this.id = id;
        setName();
        coordinates = Run.coordinatesForm.build();
        creationDate = LocalDate.now();
        setArea();
        setPopulation();
        setMetersAboveSeaLevel();
        climate = Climate.fromInput("климат");
        government = Government.fromInput("правительство");
        standardOfLiving = StandardOfLiving.fromInput("уровень жизни");
        setGovernor();
    }

    public City(long id, String name, Coordinates coordinates, LocalDate creationDate,
                Integer area, long population, Float metersAboveSeaLevel,
                Climate climate, Government government, StandardOfLiving standardOfLiving, Human governor) {
        this.id = id;
        this.name = name;
        this.coordinates = coordinates;
        this.creationDate = creationDate;
        this.area = area;
        this.population = population;
        this.metersAboveSeaLevel = metersAboveSeaLevel;
        this.climate = climate;
        this.government = government;
        this.standardOfLiving = standardOfLiving;
        this.governor = governor;
    }

    private void setName() {
        Run.inout.write("Введите название города:");
        name = ChackValues.chackValuesNull("Название города");
    }

    private void setArea() {
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
                this.area = area;
                break;
            } catch (NumberFormatException e) {
                Run.inout.write("Площадь должна быть типа Integer");
            }
        }
    }

    private void setPopulation() {
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
                this.population = population;
                break;
            } catch (NumberFormatException e) {
                Run.inout.write("Население должно быть типа long");
            }
        }
    }

    private void setMetersAboveSeaLevel() {
        Run.inout.write("Введите высоту над уровнем моря (или пустую строку для null):");
        String test = Run.inout.read();
        if (test.trim().isEmpty()) {
            this.metersAboveSeaLevel = null;
        } else {
            try {
                this.metersAboveSeaLevel = Float.parseFloat(test.replace(",", "."));
            } catch (NumberFormatException e) {
                Run.inout.write("Неверный формат числа, установлено null");
                this.metersAboveSeaLevel = null;
            }
        }
    }

    private void setGovernor() {
        Run.inout.write("Введите данные губернатора (или пустую строку для null):");
        String test = Run.inout.read();
        if (test.trim().isEmpty()) {
            this.governor = null;
        } else {
            this.governor = Run.humanForm.build();
        }
    }

    // Геттеры
    public long getId() { return id; }
    public String getName() { return name; }
    public Coordinates getCoordinates() { return coordinates; }
    public LocalDate getCreationDate() { return creationDate; }
    public Integer getArea() { return area; }
    public long getPopulation() { return population; }
    public Float getMetersAboveSeaLevel() { return metersAboveSeaLevel; }
    public Climate getClimate() { return climate; }
    public Government getGovernment() { return government; }
    public StandardOfLiving getStandardOfLiving() { return standardOfLiving; }
    public Human getGovernor() { return governor; }

    @Override
    public String toString() {
        return " | " + id + " | " + name + " | " + coordinates.getX() + ", " + coordinates.getY() +
               " | " + creationDate + " | " + area + " | " + population +
               " | " + metersAboveSeaLevel + " | " + climate +
               " | " + government + " | " + standardOfLiving +
               " | " + (governor != null ? governor.toString() : "null") + " | ";
    }

    @Override
    public int compareTo(City other) {
        int nameCompare = Long.compare(this.name.length(), other.name.length());
        if (nameCompare != 0) return nameCompare;

        int coordCompare = this.coordinates.compareTo(other.coordinates);
        if (coordCompare != 0) return coordCompare;

        int areaCompare = Integer.compare(this.area, other.area);
        if (areaCompare != 0) return areaCompare;

        return Long.compare(this.id, other.id);
    }
}
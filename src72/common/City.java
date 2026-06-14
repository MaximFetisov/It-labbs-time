package common;

import java.io.Serializable;
import java.time.LocalDate;

/**
 * Класс, представляющий город.
 * Содержит полную информацию о городе: идентификатор, название,
 * координаты, дату создания, площадь, население, высоту над уровнем моря,
 * климат, форму правления, уровень жизни и губернатора.
 * <p>
 * Реализует интерфейсы {@link Comparable} для сравнения городов
 * и {@link Serializable} для передачи по сети.
 * </p>
 *
 * @author Максим
 * @see Coordinates
 * @see Climate
 * @see Government
 * @see StandardOfLiving
 * @see Human
 */
public class City implements Comparable<City>, Serializable {
    private static final long serialVersionUID = 1L;

    private long id;
    private String name;
    private Coordinates coordinates;
    private LocalDate creationDate;
    private Integer area;
    private long population;
    private Float metersAboveSeaLevel;
    private Climate climate;
    private Government government;
    private StandardOfLiving standardOfLiving;
    private Human governor;

    /**
     * Конструктор для создания города с полным набором параметров.
     * Используется при десериализации из файла или сети.
     */
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

    /**
     * Конструктор для создания города только с ID.
     * Используется командой update для последующего заполнения через формы.
     */
    public City(long id) {
        this.id = id;
    }
    public City() {
        // Пустой конструктор для маппинга из БД
    }

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
    private String authorLogin;  // Логин создателя

    public String getAuthorLogin() { return authorLogin; }
    public void setAuthorLogin(String authorLogin) { this.authorLogin = authorLogin; }

    public void setId(long id) { this.id = id; }
    public void setName(String name) { this.name = name; }
    public void setCoordinates(Coordinates coordinates) { this.coordinates = coordinates; }
    public void setCreationDate(LocalDate creationDate) { this.creationDate = creationDate; }
    public void setArea(Integer area) { this.area = area; }
    public void setPopulation(long population) { this.population = population; }
    public void setMetersAboveSeaLevel(Float metersAboveSeaLevel) { this.metersAboveSeaLevel = metersAboveSeaLevel; }
    public void setClimate(Climate climate) { this.climate = climate; }
    public void setGovernment(Government government) { this.government = government; }
    public void setStandardOfLiving(StandardOfLiving standardOfLiving) { this.standardOfLiving = standardOfLiving; }
    public void setGovernor(Human governor) { this.governor = governor; }

    /**
     * Сравнивает города по длине названия, координатам, площади и ID.
     *
     * @param other другой город для сравнения
     * @return отрицательное, нулевое или положительное значение
     */
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

    /**
     * Возвращает строковое представление города.
     *
     * @return форматированная строка со всеми полями
     */
    @Override
    public String toString() {
        return " | " + id +
                " | " + name +
                " | " + coordinates.getX() + ", " + coordinates.getY() +
                " | " + creationDate +
                " | " + area +
                " | " + population +
                " | " + metersAboveSeaLevel +
                " | " + climate +
                " | " + government +
                " | " + standardOfLiving +
                " | " + (governor != null ? governor.toString() : "null") +
                " | АВТОР: " + authorLogin;
    }
}
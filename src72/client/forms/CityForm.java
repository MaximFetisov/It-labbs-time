package client.forms;

import common.City;
import common.Coordinates;
import common.Climate;
import common.Government;
import common.StandardOfLiving;
import common.Human;

import java.io.BufferedReader;
import java.io.IOException;
import java.time.LocalDate;
import java.util.Scanner;

/**
 * Класс-билдер для создания объектов City.
 */
public class CityForm {
    private Scanner scanner;
    private CoordinatesForm coordinatesForm;
    private HumanForm humanForm;

    public CityForm() {
        this.scanner = new Scanner(System.in);
        this.coordinatesForm = new CoordinatesForm();
        this.humanForm = new HumanForm();
    }

    public City build() {
        try {
            return new City(
                    0L,
                    setName(),
                    coordinatesForm.build(),
                    LocalDate.now(),
                    setArea(),
                    setPopulation(),
                    setMetersAboveSeaLevel(),
                    readClimate(),
                    readGovernment(),
                    readStandardOfLiving(),
                    setGovernor()
            );
        } catch (Exception e) {
            System.err.println("Ошибка при создании объекта City: " + e.getMessage());
            return null;
        }
    }

    public City buildForUpdate() {
        City city = build();
        if (city != null) {
            city.setId(0L);
        }
        return city;
    }

    /**
     * Создаёт объект City, читая параметры из файла скрипта.
     */
    public City buildFromFile(BufferedReader reader, int lineNumber) throws IOException {
        try {
            // 1. Название
            String name = readNextLine(reader, lineNumber, "название города");
            if (name == null || name.trim().isEmpty()) {
                throw new IllegalArgumentException("Название не может быть пустым");
            }

            // 2. Координаты
            String xStr = readNextLine(reader, lineNumber, "координата X");
            String yStr = readNextLine(reader, lineNumber, "координата Y");
            float x = Float.parseFloat(xStr.replace(",", "."));
            int y = Integer.parseInt(yStr.trim());
            if (x <= -872) throw new IllegalArgumentException("X должно быть > -872");
            if (y <= -846) throw new IllegalArgumentException("Y должно быть > -846");
            Coordinates coordinates = new Coordinates(x, y);

            // 3. Площадь
            String areaStr = readNextLine(reader, lineNumber, "площадь");
            int area = Integer.parseInt(areaStr.trim());
            if (area <= 0) throw new IllegalArgumentException("Площадь должна быть > 0");

            // 4. Население
            String popStr = readNextLine(reader, lineNumber, "население");
            long population = Long.parseLong(popStr.trim());
            if (population <= 0) throw new IllegalArgumentException("Население должно быть > 0");

            // 5. Высота над уровнем моря (может быть null)
            String metersStr = readNextLine(reader, lineNumber, "высота (или пусто для null)");
            Float metersAboveSeaLevel = null;
            if (metersStr != null && !metersStr.trim().isEmpty()) {
                metersAboveSeaLevel = Float.parseFloat(metersStr.replace(",", "."));
            }

            // 6. Климат
            String climateStr = readNextLine(reader, lineNumber, "климат");
            Climate climate = Climate.fromString(climateStr);
            if (climate == null) climate = Climate.getDefault();

            // 7. Правительство
            String govStr = readNextLine(reader, lineNumber, "правительство");
            Government government = Government.fromString(govStr);
            if (government == null) government = Government.getDefault();

            // 8. Уровень жизни
            String solStr = readNextLine(reader, lineNumber, "уровень жизни");
            StandardOfLiving standardOfLiving = StandardOfLiving.fromString(solStr);
            if (standardOfLiving == null) standardOfLiving = StandardOfLiving.getDefault();

            // 9. Губернатор (может быть null)
            String governorName = readNextLine(reader, lineNumber, "имя губернатора (или пусто для null)");
            Human governor = null;
            if (governorName != null && !governorName.trim().isEmpty()) {
                String governorAgeStr = readNextLine(reader, lineNumber, "возраст губернатора");
                int governorAge = Integer.parseInt(governorAgeStr.trim());
                if (governorAge <= 0) throw new IllegalArgumentException("Возраст губернатора должен быть > 0");
                governor = new Human(governorName.trim(), governorAge);
            }

            return new City(
                    0L,
                    name.trim(),
                    coordinates,
                    LocalDate.now(),
                    area,
                    population,
                    metersAboveSeaLevel,
                    climate,
                    government,
                    standardOfLiving,
                    governor
            );

        } catch (NumberFormatException e) {
            System.err.println("Ошибка в скрипте (строка ~" + lineNumber + "): неверный формат числа");
            return null;
        } catch (IllegalArgumentException e) {
            System.err.println("Ошибка в скрипте (строка ~" + lineNumber + "): " + e.getMessage());
            return null;
        }
    }

    /**
     * Вспомогательный метод: читает следующую строку из файла.
     */
    private String readNextLine(BufferedReader reader, int baseLineNumber, String fieldName) throws IOException {
        String line = reader.readLine();
        if (line == null) {
            throw new IOException("Файл скрипта закончился при чтении поля: " + fieldName);
        }
        return line.trim();
    }

    // === Методы для интерактивного ввода (без изменений) ===

    private String setName() {
        while (true) {
            System.out.print("Введите название города: ");
            String name = scanner.nextLine().trim();
            if (!name.isEmpty()) return name;
            System.out.println("Название города не может быть пустым");
        }
    }

    private int setArea() {
        while (true) {
            try {
                System.out.print("Введите площадь города: ");
                String test = scanner.nextLine().trim();
                int area = Integer.parseInt(test);
                if (area > 0) return area;
                System.out.println("Площадь должна быть больше 0");
            } catch (NumberFormatException e) {
                System.out.println("Площадь должна быть типа Integer");
            }
        }
    }

    private long setPopulation() {
        while (true) {
            try {
                System.out.print("Введите население города: ");
                String test = scanner.nextLine().trim();
                long population = Long.parseLong(test);
                if (population > 0) return population;
                System.out.println("Население должно быть больше 0");
            } catch (NumberFormatException e) {
                System.out.println("Население должно быть типа long");
            }
        }
    }

    private Float setMetersAboveSeaLevel() {
        while (true) {
            System.out.print("Введите высоту над уровнем моря (или пустая строка для null): ");
            String test = scanner.nextLine().trim();
            if (test.isEmpty()) {
                System.out.println("Установлено значение: null");
                return null;
            }
            String normalized = test.replace(",", ".");
            if (normalized.contains(".") && normalized.split("\\.")[1].length() > 2) {
                System.out.println("Слишком много знаков после запятой (макс. 2). Попробуйте снова.");
                continue;
            }
            if (normalized.length() > 10) {
                System.out.println("Число слишком большое (макс. 10 символов). Попробуйте снова.");
                continue;
            }
            try {
                return Float.parseFloat(normalized);
            } catch (NumberFormatException e) {
                System.out.println("Неверный формат числа. Попробуйте снова.");
            }
        }
    }

    private Climate readClimate() {
        System.out.println("Доступные значения: MONSOON, MEDITERRANIAN, TUNDRA");
        while (true) {
            System.out.print("Введите климат: ");
            String input = scanner.nextLine().trim();
            if (input.isEmpty()) {
                System.out.println("Климат не может быть пустым. Используется значение по умолчанию: MEDITERRANIAN");
                return Climate.getDefault();
            }
            Climate climate = Climate.fromString(input);
            if (climate != null) return climate;
            System.out.println("Неверное значение. Используется MEDITERRANIAN");
            return Climate.getDefault();
        }
    }

    private Government readGovernment() {
        System.out.println("Доступные значения: ANARCHY, CORPORATOCRACY, NOOCRACY, THALASSOCRACY");
        while (true) {
            System.out.print("Введите правительство: ");
            String input = scanner.nextLine().trim();
            if (input.isEmpty()) {
                System.out.println("Правительство не может быть пустым. Используется значение по умолчанию: NOOCRACY");
                return Government.getDefault();
            }
            Government gov = Government.fromString(input);
            if (gov != null) return gov;
            System.out.println("Неверное значение. Используется NOOCRACY");
            return Government.getDefault();
        }
    }

    private StandardOfLiving readStandardOfLiving() {
        System.out.println("Доступные значения: ULTRA_HIGH, HIGH, MEDIUM, LOW");
        while (true) {
            System.out.print("Введите уровень жизни: ");
            String input = scanner.nextLine().trim();
            if (input.isEmpty()) {
                System.out.println("Уровень жизни не может быть пустым. Используется значение по умолчанию: MEDIUM");
                return StandardOfLiving.getDefault();
            }
            StandardOfLiving sol = StandardOfLiving.fromString(input);
            if (sol != null) return sol;
            System.out.println("Неверное значение. Используется MEDIUM");
            return StandardOfLiving.getDefault();
        }
    }

    private Human setGovernor() {
        System.out.print("Введите данные губернатора (или пустая строка для null): ");
        String test = scanner.nextLine().trim();
        if (test.isEmpty()) {
            System.out.println("Губернатор не указан (null)");
            return null;
        } else {
            System.out.println("Ввод данных губернатора:");
            return humanForm.build();
        }
    }
}
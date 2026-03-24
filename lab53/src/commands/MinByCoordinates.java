package commands;

import datass.City;
import datass.Coordinates;
import interfaces.Command;
import main.Run;

/**
 * Команда для вывода объекта из коллекции с минимальными координатами.
 * Сравнивает элементы по полям coordinates.x и coordinates.y с помощью
 * метода {@link Coordinates#compareTo(Coordinates)}.
 * Если коллекция пуста, выводит соответствующее сообщение.
 *
 * @author Максим
 * @see Command
 * @see City
 * @see Coordinates
 * @see Run#collectionManager
 */
public class MinByCoordinates implements Command {
    @Override
    public boolean execute(String[] args) {
        try {
            if (Run.collectionManager.getCities().isEmpty()) {
                Run.inout.write("Коллекция пуста. Невозможно определить минимальный элемент.");
                return true;
            }

            City minCity = null;
            Coordinates minCoords = null;

            for (City city : Run.collectionManager.getCities()) {
                if (minCoords == null || city.getCoordinates().compareTo(minCoords) < 0) {
                    minCity = city;
                    minCoords = city.getCoordinates();
                }
            }

            if (minCity == null) {
                Run.inout.write("Не удалось найти элемент с минимальными координатами.");
                return true;
            }

            Run.inout.write("=== Элемент с минимальными координатами ===");
            Run.inout.write("ID: " + minCity.getId());
            Run.inout.write("Название: " + minCity.getName());
            Run.inout.write("Координаты: X=" + minCity.getCoordinates().getX() + ", Y=" + minCity.getCoordinates().getY());
            Run.inout.write("Полная информация:");
            Run.inout.write(minCity.toString());
            Run.inout.write("==========================================");
            return true;

        } catch (Exception e) {
            Run.inout.write("Ошибка при поиске элемента с минимальными координатами: " + e.getMessage());
            Run.inout.setScriptError(true);
            return false;
        }
    }

    @Override
    public String getDescription() {
        return "Вывести любой объект из коллекции, значение поля coordinates которого является минимальным";
    }

    @Override
    public String getName() {
        return "min_by_coordinates";
    }
}
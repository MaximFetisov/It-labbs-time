package servers.managers;

import common.City;
import common.Human;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class CollectionManager {
    private static final Logger logger = Logger.getLogger(CollectionManager.class.getName());

    private final List<City> cities = new ArrayList<>();

    // Синхронизации чтения и записи
    private final ReadWriteLock rwLock = new ReentrantReadWriteLock();

    private long nextId;
    private final LocalDateTime initializationDate;
    private final Deque<String> commandHistory;
    private static final int MAX_HISTORY_SIZE = 9;

    private final DatabaseManager databaseManager;

    public CollectionManager(DatabaseManager dbManager) {
        this.databaseManager = dbManager;
        this.nextId = 1L;
        this.commandHistory = new LinkedList<>();
        this.initializationDate = LocalDateTime.now();
    }

    public void refreshFromDb() {
        rwLock.writeLock().lock();
        try {
            ArrayList<City> fromDb = databaseManager.loadAllCities();
            cities.clear();
            cities.addAll(fromDb);

            if (!cities.isEmpty()) {
                long maxId = cities.stream()
                        .mapToLong(City::getId)
                        .max()
                        .orElse(0L);
                this.nextId = maxId + 1;
            }
            logger.info("Коллекция загружена из БД: " + cities.size() + " элементов");
        } finally {
            rwLock.writeLock().unlock();
        }
    }

    public ArrayList<City> getCities() {
        rwLock.readLock().lock();
        try {
            return new ArrayList<>(cities);
        } finally {
            rwLock.readLock().unlock();
        }
    }

    public City addCity(City city, String author) {
        long newId = databaseManager.addCity(city, author);
        if (newId > 0) {
            city.setId(newId);
            city.setAuthorLogin(author);
            rwLock.writeLock().lock();
            try {
                cities.add(city);
                logger.info("Элемент добавлен в кэш с ID: " + newId);
            } finally {
                rwLock.writeLock().unlock();
            }
            return city;
        }
        logger.severe("Не удалось добавить элемент в БД");
        return null;
    }

    public boolean removeById(long id, String author) {
        if (databaseManager.deleteCity(id, author)) {
            rwLock.writeLock().lock();
            try {
                cities.removeIf(c -> c.getId() == id);
                logger.info("Элемент с ID " + id + " удалён из кэша");
            } finally {
                rwLock.writeLock().unlock();
            }
            return true;
        }
        logger.warning("Не удалось удалить элемент с ID " + id);
        return false;
    }

    public boolean updateCity(long id, City newCity, String author) {
        if (databaseManager.updateCity(id, newCity, author)) {
            newCity.setId(id);
            newCity.setAuthorLogin(author);
            rwLock.writeLock().lock();
            try {
                for (int i = 0; i < cities.size(); i++) {
                    if (cities.get(i).getId() == id) {
                        cities.set(i, newCity);
                        break;
                    }
                }
                logger.info("Элемент с ID " + id + " обновлён в кэше");
            } finally {
                rwLock.writeLock().unlock();
            }
            return true;
        }
        logger.warning("Не удалось обновить элемент с ID " + id);
        return false;
    }

    public int clearOwn(String author) {
        int deleted = databaseManager.clearDB(author);
        if (deleted > 0) {
            refreshFromDb();
        }
        return deleted;
    }

    public boolean removeFirstOwn(String author) {
        rwLock.readLock().lock();
        City firstOwnCity;
        try {
            firstOwnCity = cities.stream()
                    .filter(city -> city.getAuthorLogin().equals(author))
                    .findFirst()
                    .orElse(null);
        } finally {
            rwLock.readLock().unlock();
        }

        if (firstOwnCity != null) {
            return removeById(firstOwnCity.getId(), author);
        }
        return false;
    }

    public int removeLowerOwn(City referenceCity, String author) {
        List<Long> idsToRemove;
        rwLock.readLock().lock();
        try {
            idsToRemove = cities.stream()
                    .filter(city -> city.getAuthorLogin().equals(author))
                    .filter(city -> city.compareTo(referenceCity) < 0)
                    .map(City::getId)
                    .collect(Collectors.toList());
        } finally {
            rwLock.readLock().unlock();
        }

        int removedCount = 0;
        for (Long id : idsToRemove) {
            if (removeById(id, author)) {
                removedCount++;
            }
        }
        return removedCount;
    }

    public int getSize() {
        rwLock.readLock().lock();
        try {
            return cities.size();
        } finally {
            rwLock.readLock().unlock();
        }
    }

    public void clear() {
        rwLock.writeLock().lock();
        try {
            cities.clear();
        } finally {
            rwLock.writeLock().unlock();
        }
    }

    public City getById(long id) {
        rwLock.readLock().lock();
        try {
            return cities.stream()
                    .filter(city -> city.getId() == id)
                    .findFirst()
                    .orElse(null);
        } finally {
            rwLock.readLock().unlock();
        }
    }

    public double getSumOfMeters() {
        rwLock.readLock().lock();
        try {
            return cities.stream()
                    .mapToDouble(city -> city.getMetersAboveSeaLevel() != null ?
                            city.getMetersAboveSeaLevel() : 0.0)
                    .sum();
        } finally {
            rwLock.readLock().unlock();
        }
    }

    public City getMinByCoordinates() {
        rwLock.readLock().lock();
        try {
            return cities.stream()
                    .min(Comparator.comparing(City::getCoordinates))
                    .orElse(null);
        } finally {
            rwLock.readLock().unlock();
        }
    }

    public ArrayList<Human> getUniqueGovernors() {
        rwLock.readLock().lock();
        try {
            return cities.stream()
                    .map(City::getGovernor)
                    .filter(gov -> gov != null)
                    .distinct()
                    .collect(Collectors.toCollection(ArrayList::new));
        } finally {
            rwLock.readLock().unlock();
        }
    }

    @Deprecated
    public void setCities(ArrayList<City> newCities) {
        refreshFromDb();
    }

    public void addToHistory(String commandName) {
        rwLock.writeLock().lock();
        try {
            if (commandHistory.size() >= MAX_HISTORY_SIZE) {
                commandHistory.removeFirst();
            }
            commandHistory.addLast(commandName);
        } finally {
            rwLock.writeLock().unlock();
        }
    }

    public Deque<String> getCommandHistory() {
        rwLock.readLock().lock();
        try {
            return new LinkedList<>(commandHistory);
        } finally {
            rwLock.readLock().unlock();
        }
    }

    public void clearHistory() {
        rwLock.writeLock().lock();
        try {
            commandHistory.clear();
        } finally {
            rwLock.writeLock().unlock();
        }
    }

    public LocalDateTime getInitializationDate() {
        return initializationDate;
    }
}
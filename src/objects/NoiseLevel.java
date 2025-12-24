package objects;

import java.util.ArrayList;
import java.util.List;
import interfaces.*;

public class NoiseLevel {
    private static NoiseLevel instance;
    private int level;
    private final List<NoiseListener> listeners = new ArrayList<>();

    private NoiseLevel() {
        this.level = 0;
    }

    public static NoiseLevel getInstance() {
        if (instance == null) {
            instance = new NoiseLevel();
        }
        return instance;
    }

    public void addListener(NoiseListener listener) {
        listeners.add(listener);
    }

    private void notifyListeners() {
        for (NoiseListener listener : listeners) {
            listener.onNoiseChanged(level);
        }
    }

    public void increase(int amount) {
        level += amount;
        System.out.println("Уровень шума повысился до " + level);
        notifyListeners();
    }

    public void decrease(int amount) {
        level = Math.max(0, level - amount);
        System.out.println("Уровень шума снизился до " + level);
        notifyListeners();
    }

    public int getLevel() {
        return level;
    }

    public void reset() {
        level = 0;
        notifyListeners();
    }
}
package objects;

import java.util.Random;
import enums.*;
import interfaces.*;
import exceptions.MealNotReadyException;

public class FrekenBok extends Person {
    private boolean isSinging = false;
    private final Random random = new Random();

    public FrekenBok(String name, Mood mood) {
        super(name, mood);
    }

    public void sing() {
        if (mood == Mood.HAPPY) {
            isSinging = true;
            System.out.println(name + " поёт громко: \"Ах, Фрида, это было бы для тебя лучше!..\"");
            NoiseLevel.getInstance().increase(30);
        }
    }

    public void stopSinging() {
        if (isSinging) {
            isSinging = false;
            System.out.println(name + " замолкает.");
            NoiseLevel.getInstance().decrease(30);
        }
    }

    public void cook(String dishName) throws MealNotReadyException {
        Doneness doneness = Doneness.values()[random.nextInt(Doneness.values().length)];
        Meal meal = new Meal(dishName, doneness);
        System.out.println(name + " жарит " + meal.name() + " (" + meal.doneness() + ").");
        NoiseLevel.getInstance().increase(20);

        switch (meal.doneness()) {
            case RAW -> {
                mood = Mood.ANGRY;
                throw new MealNotReadyException(dishName);
            }
            case MEDIUM -> mood = Mood.HAPPY;
            case BURNT -> mood = Mood.SAD;
        }
    }

    @Override
    public void speak() {
        if (mood == Mood.HAPPY) {
            System.out.println(name + " весело напевает.");
        } else if (mood == Mood.ANGRY) {
            System.out.println(name + " сердито молчит.");
        } else {
            System.out.println(name + " выглядит усталой и молча.");
        }
    }
}
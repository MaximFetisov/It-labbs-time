package objects;

import enums.*;
import interfaces.*;
public class Karlson extends Person {
    public Karlson(String name, Mood mood) {
        super(name, mood);
    }

    public void flyIn() {
        System.out.println(name + " влетает в кухню с криком: \"Привет, Фрекен Бок!\"");
        NoiseLevel.getInstance().increase(50);
    }

    @Override
    public void speak() {
        if (mood == Mood.HAPPY) {
            System.out.println(name + " восклицает: \"Эскалопы от Фрекен Бок! лучшие в мире!\"");
        } else {
            System.out.println(name + " говорит тихо: \"...это было вкусно.\"");
        }
    }
}
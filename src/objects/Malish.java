package objects;

import enums.*;
import interfaces.*;

public class Malish extends Person implements NoiseListener {
    private boolean isAwake = false;
    private boolean haspet;
    
    @Override
    public void speak() {
    if (!isAwake) {
        System.out.println(name + " спит и не говорит.");
    } else if (mood == Mood.HAPPY) {
        System.out.println(name + " говорит: \"Мне было так уютно! Все такие добрые сегодня.\"");
    } else if (mood == Mood.SAD) {
        System.out.println(name + " тихо говорит: \"Почему все такие грустные?..\"");
    } else {
        System.out.println(name + " зевает и бормочет что-то неразборчивое.");
    }
}

    public Malish(String name, Mood mood, boolean hasPet) {
        super(name, mood);
        NoiseLevel.getInstance().addListener(this);
    }

    @Override
    public void onNoiseChanged(int newLevel) {
        if (!isAwake && newLevel >= 70) {
            isAwake = true;
            mood = Mood.HAPPY;
            System.out.println(name + " проснулся от шума.");
        }
    }

    public boolean isAwake() { return isAwake; }
    
}
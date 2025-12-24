package objects;

import enums.*;
import interfaces.*;

public class UncleJulius extends Person {
    public UncleJulius(String name, Mood mood) {
        super(name, mood);
    }

    @Override
    public void speak() {
        System.out.println(name + " говорит: \"Что за шум был ночью?\"");
    }
}
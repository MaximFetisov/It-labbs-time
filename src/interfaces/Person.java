package interfaces;

import java.util.Objects;
import enums.Mood;

public abstract class Person {
    protected String name;
    protected Mood mood;

    public Person(String name, Mood mood) {
        this.name = name;
        this.mood = mood;
    }

    public String getName() { return name; }
    public Mood getMood() { return mood; }
    public void setMood(Mood mood) { this.mood = mood; }

    public void eat() {
        System.out.println(name + " ест с удовольствием.");
    }

    public abstract void speak();

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Person p)) return false;
        return Objects.equals(name, p.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }

    @Override
    public String toString() {
        return name + " (настроение: " + mood + ")";
    }
}
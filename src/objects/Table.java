package objects;

import java.util.ArrayList;
import java.util.List;

import interfaces.*;
import exceptions.*;

public class Table {
    private List<Person> guests = new ArrayList<>();

    public void addGuest(Person person) {
        guests.add(person);
        System.out.println(person.getName() + " садится за стол.");
    }

    public Person findGuestByName(String name) throws CharacterNotFoundException {
        for (Person p : guests) {
            if (p.getName().equals(name)) {
                return p;
            }
        }
        throw new CharacterNotFoundException(name);
    }

    public void startDinner() {
        System.out.println("Все сидят вместе и обсуждают события этой ночи.");
        for (Person p : guests) {
            p.speak();
            p.eat();
        }
    }
}
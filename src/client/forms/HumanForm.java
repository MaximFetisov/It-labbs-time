package client.forms;

import common.Human;
import java.util.Scanner;

public class HumanForm {
    private Scanner scanner;

    public HumanForm() {
        this.scanner = new Scanner(System.in);
    }

    public Human build() {
        return new Human(setName(), setAge());
    }

    private String setName() {
        while (true) {
            System.out.print("Введите имя губернатора: ");
            String name = scanner.nextLine().trim();
            if (!name.isEmpty()) {
                return name;
            }
            System.out.println("Имя не может быть пустым");
        }
    }

    private int setAge() {
        while (true) {
            try {
                System.out.print("Введите возраст губернатора: ");
                String testAge = scanner.nextLine().trim();
                int age = Integer.parseInt(testAge);
                if (age > 0) {
                    return age;
                }
                System.out.println("Возраст должен быть больше 0");
            } catch (NumberFormatException e) {
                System.out.println("Возраст должен быть типа Integer");
            }
        }
    }
}
package client.forms;

import common.Coordinates;
import java.util.Scanner;

public class CoordinatesForm {
    private Scanner scanner;

    public CoordinatesForm() {
        this.scanner = new Scanner(System.in);
    }

    public Coordinates build() {
        return new Coordinates(setX(), setY());
    }

    private Float setX() {
        while (true) {
            System.out.print("Введите значение X (больше -872): ");
            String testX = scanner.nextLine().trim();
            String normalizedX = testX.replace(",", ".");

            try {
                if (normalizedX.contains(".") && normalizedX.split("\\.")[1].length() > 2) {
                    throw new NumberFormatException("Слишком много знаков после запятой");
                }

                float x = Float.parseFloat(normalizedX);
                if (x <= -872) {
                    System.out.println("X должно быть больше -872");
                } else if (normalizedX.length() > 10) {
                    System.out.println("Число слишком большое (макс. 10 символов)");
                    continue;
                } else {
                    return x;
                }
            } catch (NumberFormatException e) {
                System.out.println("X должно быть типа float (макс. 2 знака после запятой)");
            }
        }
    }

    private Integer setY() {
        while (true) {
            try {
                System.out.print("Введите значение Y (больше -846): ");
                String testY = scanner.nextLine().trim();
                int y = Integer.parseInt(testY);
                if (y <= -846) {
                    System.out.println("Y должно быть больше -846");
                } else {
                    return y;
                }
            } catch (NumberFormatException e) {
                System.out.println("Y должно быть типа int");
            }
        }
    }
}
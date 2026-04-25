package main;

/**
 * Главный класс для запуска приложения.
 * Содержит точку входа {@link #main(String[])} и создаёт экземпляр {@link Run}.
 *
 * @author Максим
 * @see Run
 */
public class Main {

    /**
     * Точка входа в приложение.
     * Создаёт экземпляр {@link Run} и запускает основной цикл программы.
     *
     * @param args аргументы командной строки (должны содержать имя файла)
     */
    public static void main(String[] args) {
        Run run = new Run();
        run.run(args);
    }
}
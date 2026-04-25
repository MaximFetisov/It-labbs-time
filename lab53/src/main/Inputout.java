package main;


import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.Scanner;

public class Inputout {
    private Scanner scan;
    private Scanner fileScan;
    private boolean readFromFile;
    private boolean scriptHasError;
    public Inputout() {
        scan = new Scanner(System.in);
        fileScan = null;
        readFromFile = false;
        scriptHasError = false;
    }

    /**
     * Выводит сообщение в стандартный поток вывода.
     *
     * @param line текст сообщения
     */
    public void write(String line) {
        System.out.println(line);
    }

    /**
     * Считывает одну строку ввода.
     * Источник ввода зависит от режима: консоль или файл.
     *
     * @return считанная строка
     */
    public String read() {
        if (readFromFile)
            {
            if (fileScan != null && fileScan.hasNextLine()) {
                return fileScan.nextLine();
            } else {
                write("Даные из скрипта внесены в коллецкцию. Возврат в консоль. Продолжите ввод");
                stopFileReading();
                return read();
            }
        } else {
            if (scan.hasNextLine()) {
                return scan.nextLine();
            } else {
                write("Вы нажали ctrl+D. Завершение программы");
                scan.close();
                System.exit(0);
                return "";
            }
        }
    }

    /**
     * Переключает режим ввода на чтение из указанного файла.
     * Если файл не найден, выводит сообщение об ошибке и остаётся в режиме консоли.
     *
     * @param filename имя файла для чтения (путь к файлу)
     */
    public void startFileReading(String filename) {
        try {
            File file = new File(filename);
            if (!file.exists()) {
                write("Файл не найден: " + filename);
                readFromFile = false;
                return;
            }
            if (!file.canRead()) {
                write("Нет прав на чтение файла: " + filename);
                readFromFile = false;
                return;
            }
            fileScan = new Scanner(new FileReader(file));
            readFromFile = true;
            scriptHasError = false;
        } catch (FileNotFoundException e) {
            write("Файл не найден: " + filename);
            readFromFile = false;
        }
    }

    /**
     * Завершает режим чтения из файла.
     */
    public void stopFileReading() {
        if (fileScan != null) {
            fileScan.close();
            fileScan = null;
        }
        readFromFile = false;
    }

    /**
     * Проверяет, есть ли ещё строки для чтения в текущем источнике.
     * В режиме файла проверяет, остались ли непрочитанные строки.
     *
     * @return true, если есть следующая строка, иначе false
     */
    public boolean hasNextLine() {
        if (readFromFile) {
            return fileScan != null && fileScan.hasNextLine();
        }
        return true;
    }

    /**
     * Устанавливает флаг ошибки выполнения скрипта.
     *
     * @param error true, если ошибка произошла, иначе false
     */
    public void setScriptError(boolean error) {
        scriptHasError = error;
    }

    /**
     * Проверяет, была ли ошибка при выполнении скрипта.
     *
     * @return true, если ошибка была, иначе false
     */
    public boolean isScriptHasError() {
        return scriptHasError;
    }

    /**
     * Закрывает сканер консоли.
     */
    public void closeScan() {
        if (scan != null) {
            scan.close();
        }
    }

    /**
     * Проверяет, активен ли режим чтения из файла.
     *
     * @return true, если чтение из файла активно, иначе false
     */
    public boolean isReadingFromFile() {
        return readFromFile;
    }
}

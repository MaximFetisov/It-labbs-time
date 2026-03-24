package main;

import datass.*;

import java.io.IOException;

/**
 * Главный класс приложения, содержащий точку входа и управляющий основным циклом программы.
 * Хранит статические ссылки на все основные компоненты системы:
 * <ul>
 *   <li>{@link #inout} - менеджер ввода-вывода</li>
 *   <li>{@link #consoleParser} - менеджер команд</li>
 *   <li>{@link #collectionManager} - менеджер коллекции</li>
 *   <li>{@link #fileParser} - менеджер файлов</li>
 *   <li>{@link #generate} - генератор ID</li>
 *   <li>{@link #nameFile} - имя текущего рабочего файла</li>
 *   <li>{@link #commandHistory} - история последних 9 команд</li>
 * </ul>
 * Все компоненты доступны статически, что позволяет командам обращаться к ним без передачи ссылок.
 *
 * @author Максим
 * @see Inputout
 * @see ConsoleParser
 * @see CollectionManager
 * @see FileParser
 * @see Generate
 */
public class Run {
   
    public static Inputout inout;
    public static ConsoleParser consoleParser;
    public static CollectionManager collectionManager;
    public static FileParser fileParser;
    public static String nameFile;
    public static java.util.Deque<String> commandHistory;
    public static CityForm cityForm;
    public static CoordinatesForm coordinatesForm;
    public static HumanForm humanForm;
    public static Generate generate;

    public Run() {
        inout = new Inputout();
        consoleParser = new ConsoleParser();
        collectionManager = new CollectionManager();
        fileParser = new FileParser();
        generate = new Generate();
        commandHistory = new java.util.ArrayDeque<>(9);
        cityForm= new CityForm();
        coordinatesForm = new CoordinatesForm();
        humanForm = new HumanForm();
    }

    /**
     * Запускает основной цикл программы.
     * Проверяет аргументы командной строки, загружает данные из файла,
     * затем переходит в интерактивный режим ожидания команд.
     *
     * @param args аргументы командной строки (должны содержать имя файла)
     * @throws IOException 
     */
    public void run(String[] args) {
        if (args.length == 0) {
            inout.write("Ошибка: не указано имя файла.");
            inout.write("Использование: java Main <file.json>");
            nameFile = "file.json";
        } else {
            nameFile = args[0];
            inout.write("Добро пожаловать в менеджер коллекции City!");
            inout.write("Файл для работы: " + nameFile);
           
        }
        try{    
           Run.collectionManager.setCities(fileParser.readFromJsonFile(nameFile, City.class));
           Run.generate.setId(Run.collectionManager.getCities().get(Run.collectionManager.getSize()-1).getId()+1);
        } catch (IOException e){
            Run.inout.write("Чтение из файла не удалось.");
        }
        

        inout.write("Введите 'help', чтобы узнать о доступных командах.");
        while (true) {
            inout.write("Введите команду:");
            String line = inout.read().trim();

            if (line.isEmpty()) {
                continue;
            }

            String[] tokens = line.split("\\s+", 2);
            String commandName = tokens[0];
            String commandArgs = tokens.length > 1 ? tokens[1] : "";

            boolean success =consoleParser.parse(commandName, commandArgs);

            if (!success) {
                inout.write("Такой команды не существует. Введите 'help' для справки.");
            } else {
                // Добавляем команду в историю (только имя, без аргументов)
                addToHistory(commandName);
            }
        }
    }

    /**
     * Добавляет имя команды в историю (максимум 9 последних команд).
     *
     * @param commandName имя выполненной команды
     */
    public static void addToHistory(String commandName) {
        if (commandHistory.size() >= 9) {
            commandHistory.removeFirst();
        }
        commandHistory.addLast(commandName);
    }

    /**
     * Возвращает историю последних команд.
     *
     * @return deque с именами команд
     */
    public static java.util.Deque<String> getCommandHistory() {
        return commandHistory;
    }
}
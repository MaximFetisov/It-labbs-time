package interfaces;

/**
 * Базовый интерфейс для всех команд.
 * Все конкретные команды должны реализовывать этот интерфейс
 * и определять логику выполнения в методе {@link #execute(String[])}.
 *
 
 * @see commands.Add
 * @see commands.Show
 * @see commands.Help
 */
public interface Command {
    /**
     * Выполняет команду с указанными аргументами.
     *
     * @param args аргументы команды (например, id для update, имя файла для execute_script)
     * @return true, если команда выполнена успешно, иначе false
     */
    boolean execute(String[] args);

    /**
     * Возвращает описание команды для справки.
     * Используется командой help для вывода информации о доступных командах.
     *
     * @return описание команды (формат: "имя команды : описание")
     */
    default String getDescription() {
        return "Нет описания";
    }

    /**
     * Возвращает имя команды.
     * Используется для регистрации команды в Parser.
     *
     * @return имя команды (например, "help", "show", "add")
     */
    default String getName() {
        return "unknown";
    }
}
package common;


import java.io.Serializable;

/**
 * Объект запроса от клиента к серверу.
 * Содержит тип команды, аргументы и объект City (для команд add/update).
 * Все поля сериализуются для передачи по сети через UDP.
 *
 * @author Максим
 * @see Serializable
 * @see CommandType
 * @see City
 */
public class CommandRequest implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private CommandType type;
    private String[] arguments;
    private City city;
    
    /**
     * Создаёт новый запрос команды.
     *
     * @param type тип команды
     * @param arguments аргументы команды (может быть null или пустым массивом)
     * @param city объект City для команд add/update (может быть null)
     */
    public CommandRequest(CommandType type, String[] arguments, City city) {
        this.type = type;
        this.arguments = arguments != null ? arguments : new String[0];
        this.city = city;
    }
    
    /**
     * Возвращает тип команды.
     *
     * @return тип команды
     */
    public CommandType getType() {
        return type;
    }
    
    /**
     * Возвращает аргументы команды.
     *
     * @return массив аргументов
     */
    public String[] getArguments() {
        return arguments;
    }
    
    /**
     * Возвращает объект City (для add/update).
     *
     * @return объект City или null
     */
    public City getCity() {
        return city;
    }
    public String getFirstArgument() {
        return arguments.length > 0 ? arguments[0] : null;
    }
    public boolean hasArguments() {
        return arguments != null && arguments.length > 0;
    }
    @Override
    public String toString() {
        return "CommandRequest{type=" + type + ", args=" + 
               (arguments != null ? arguments.length : 0) + "}";
    }
}
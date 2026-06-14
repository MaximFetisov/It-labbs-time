package common;

import java.io.Serializable;

public class CommandRequest implements Serializable {
    private static final long serialVersionUID = 1L;

    private CommandType type;
    private String[] arguments;
    private City city;

    // Новые поля для авторизации
    private String login;
    private String password;

    public CommandRequest(CommandType type, String[] arguments, City city, String login, String password) {
        this.type = type;
        this.arguments = arguments;
        this.city = city;
        this.login = login;
        this.password = password;
    }

    // Геттеры
    public CommandType getType() { return type; }
    public String[] getArguments() { return arguments; }
    public City getCity() { return city; }
    public String getLogin() { return login; }
    public String getPassword() { return password; }

    public boolean hasArguments() {
        return arguments != null && arguments.length > 0;
    }

    public String getFirstArgument() {
        return hasArguments() ? arguments[0] : null;
    }
}
package client.managers;

import common.CommandType;

/**
 * Менеджер для валидации введённых пользователем команд.
 *
 * @author Максим
 */
public class ValidationManager {

    /**
     * Преобразует строку в тип команды.
     *
     * @param commandName имя команды
     * @return CommandType или UNKNOWN
     */

    /**
     * Проверяет, доступна ли команда на клиенте.
     *
     * @param type тип команды
     * @return true если команда доступна
     */
    public boolean isCommandAvailable(CommandType type) {
        return type != CommandType.SAVE;
    }

    /**
     * Проверяет корректность аргументов для команды.
     *
     * @param type тип команды
     * @param args аргументы
     * @return true если аргументы корректны
     */
    public boolean validateArguments(CommandType type, String[] args) {
        switch (type) {
            case REMOVE_BY_ID:
            case UPDATE:
                return args.length > 0 && isLong(args[0]);
            case EXECUTE_SCRIPT:
                return args.length > 0;
            case ADD:
            case CLEAR:
            case HELP:
            case INFO:
            case SHOW:
            case HISTORY:
            case MIN_BY_COORDINATES:
            case PRINT_UNIQUE_GOVERNOR:
            case REMOVE_LOWER:
            case REMOVE_FIRST:
            case SUM_OF_METERS_ABOVE_SEA_LEVEL:
                return args.length == 0;
            default:
                return true;
        }
    }

    /**
     * Проверяет, является ли строка числом long.
     */
    private boolean isLong(String str) {
        try {
            Long.parseLong(str);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }
}
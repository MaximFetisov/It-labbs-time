package common;

public enum CommandType {
    HELP, INFO, SHOW, CLEAR, EXIT,
    ADD, UPDATE, REMOVE_BY_ID, REMOVE_FIRST, REMOVE_LOWER,
    SAVE, HISTORY, NEVER, SUM_OF_METERS_ABOVE_SEA_LEVEL,
    MIN_BY_COORDINATES, PRINT_UNIQUE_GOVERNOR,
    EXECUTE_SCRIPT,
    LOGIN, REGISTER,LOGOUT,
    UNKNOWN;

    public static CommandType parseCommand(String commandName) {
        try {
            return CommandType.valueOf(commandName.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            return CommandType.UNKNOWN;
        }
    }
}
package exceptions;

public class CharacterNotFoundException extends Exception {
    public CharacterNotFoundException(String name) {
        super("Персонаж не найден: " + name);
    }

    @Override
    public String getMessage() {
        return "CharacterNotFoundException: " + super.getMessage();
    }
}
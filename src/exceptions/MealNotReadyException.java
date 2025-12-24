package exceptions;

public class MealNotReadyException extends Exception {
    public MealNotReadyException(String dishName) {
        super("Блюдо \"" + dishName + "\" не готово к подаче!");
    }

    @Override
    public String getMessage() {
        return "MealNotReadyException: " + super.getMessage();
    }
}
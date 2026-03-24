package datass;

import main.ChackValues;
import main.Run;

/**
 * Перечисление возможных климатических зон города.
 * Содержит три варианта климата: муссонный, средиземноморский и тундровый.
 *
 * @author Максим
 * @see Government
 * @see StandardOfLiving
 */
public enum Climate {
    MONSOON,
    MEDITERRANIAN,
    TUNDRA;

    /**
     * Запрашивает и возвращает значение климата от пользователя.
     * При некорректном вводе возвращает значение по умолчанию (MEDITERRANIAN).
     *
     * @param fieldName имя поля для вывода в приглашении
     * @return выбранное значение Climate
     */
    public static Climate fromInput(String fieldName) {
        Run.inout.write("Введите " + fieldName + ":");
        Run.inout.write("Доступные значения: MONSOON, MEDITERRANIAN, TUNDRA");

        while (true) {
            String input = ChackValues.chackValuesNull(fieldName);
            try {
                return Climate.valueOf(input.trim().toUpperCase());
            } catch (IllegalArgumentException e) {
                Run.inout.write("Введеное неверное значение. Было выбрано занчение по умолчанию: MEDITERRANIAN");
                return Climate.valueOf("MEDITERRANIAN");
            }
        }
    }
}
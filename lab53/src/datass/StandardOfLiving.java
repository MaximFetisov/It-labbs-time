package datass;

import main.ChackValues;
import main.Run;

/**
 * Перечисление возможных уровней жизни города.
 * Содержит четыре варианта: ультра-высокий, высокий, средний и низкий.
 *
 * @author Максим
 * @see Climate
 * @see Government
 */
public enum StandardOfLiving {
    ULTRA_HIGH,
    HIGH,
    MEDIUM,
    LOW;

    /**
     * Запрашивает и возвращает значение уровня жизни от пользователя.
     * При некорректном вводе возвращает значение по умолчанию (MEDIUM).
     *
     * @param fieldName имя поля для вывода в приглашении
     * @return выбранное значение StandardOfLiving
     */
    public static StandardOfLiving fromInput(String fieldName) {
        Run.inout.write("Введите " + fieldName + ":");
        Run.inout.write("Доступные значения: ULTRA_HIGH, HIGH, MEDIUM, LOW");

        while (true) {
            String input = ChackValues.chackValuesNull(fieldName);
            try {
                return StandardOfLiving.valueOf(input.trim().toUpperCase());
            } catch (IllegalArgumentException e) {
                Run.inout.write("Введеное неверное значение. Было выбрано занчение по умолчанию: MEDIUM");
                return StandardOfLiving.valueOf("MEDIUM");
            }
        }
    }
}
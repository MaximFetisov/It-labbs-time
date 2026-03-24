package datass;

import main.ChackValues;
import main.Run;

/**
 * Перечисление возможных форм правления города.
 * Содержит четыре варианта: анархия, корпоратократия, ноократия и талассократия.
 *
 * @author Максим
 * @see Climate
 * @see StandardOfLiving
 */
public enum Government {
    ANARCHY,
    CORPORATOCRACY,
    NOOCRACY,
    THALASSOCRACY;

    /**
     * Запрашивает и возвращает значение формы правления от пользователя.
     * При некорректном вводе возвращает значение по умолчанию (NOOCRACY).
     *
     * @param fieldName имя поля для вывода в приглашении
     * @return выбранное значение Government
     */
    public static Government fromInput(String fieldName) {
        Run.inout.write("Введите " + fieldName + ":");
        Run.inout.write("Доступные значения: ANARCHY, CORPORATOCRACY, NOOCRACY, THALASSOCRACY");

        while (true) {
            String input = ChackValues.chackValuesNull(fieldName);
            try {
                return Government.valueOf(input.trim().toUpperCase());
            } catch (IllegalArgumentException e) {
                Run.inout.write("Введеное неверное значение. Было выбрано занчение по умолчанию: NOOCRACY");
                return Government.valueOf("NOOCRACY");
            }
        }
    }
}
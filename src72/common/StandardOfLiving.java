package common;

import java.io.Serializable;

/**
 * Перечисление возможных уровней жизни города.
 * Содержит четыре варианта: ультра-высокий, высокий, средний и низкий.
 * <p>
 * Реализует {@link Serializable} для передачи по сети между клиентом и сервером.
 * </p>
 *
 * @author Максим
 * @see Climate
 * @see Government
 */
public enum StandardOfLiving implements Serializable {
    ULTRA_HIGH,
    HIGH,
    MEDIUM,
    LOW;

    /**
     * Преобразует строку в значение уровня жизни.
     * Поддерживает ввод в русской раскладке (автоматическая конвертация).
     *
     * @param value строковое представление уровня жизни
     * @return соответствующее значение StandardOfLiving или null
     */
    public static StandardOfLiving fromString(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }

        String input = value.trim().toUpperCase();

        try {
            return StandardOfLiving.valueOf(input);
        } catch (IllegalArgumentException e) {
            // Конвертировать раскладку
            String converted = KeyboardLayoutConverter.convertRussianToEnglish(input);
            try {
                return StandardOfLiving.valueOf(converted);
            } catch (IllegalArgumentException e2) {
                return null;
            }
        }
    }

    /**
     * Возвращает уровень жизни по умолчанию (MEDIUM).
     * Используется при некорректном вводе.
     *
     * @return значение по умолчанию
     */
    public static StandardOfLiving getDefault() {
        return MEDIUM;
    }
}
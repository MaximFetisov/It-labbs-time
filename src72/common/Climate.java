package common;

import java.io.Serializable;

/**
 * Перечисление возможных климатических зон города.
 * Содержит три варианта климата: муссонный, средиземноморский и тундровый.
 * <p>
 * Реализует {@link Serializable} для передачи по сети между клиентом и сервером.
 * </p>
 *
 * @author Максим
 * @see Government
 * @see StandardOfLiving
 */
public enum Climate implements Serializable {
    MONSOON,
    MEDITERRANIAN,
    TUNDRA;

    /**
     * Преобразует строку в значение климата.
     * Поддерживает ввод в русской раскладке (автоматическая конвертация).
     *
     * @param value строковое представление климата
     * @return соответствующее значение Climate или null
     */
    public static Climate fromString(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }

        String input = value.trim().toUpperCase();

        try {
            return Climate.valueOf(input);
        } catch (IllegalArgumentException e) {
            // Конвертировать раскладку)
            String converted = KeyboardLayoutConverter.convertRussianToEnglish(input);
            try {
                return Climate.valueOf(converted);
            } catch (IllegalArgumentException e2) {
                return null;
            }
        }
    }

    /**
     * Возвращает климат по умолчанию (MEDITERRANIAN).
     * Используется при некорректном вводе.
     *
     * @return значение по умолчанию
     */
    public static Climate getDefault() {
        return MEDITERRANIAN;
    }
}
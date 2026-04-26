package common;

import java.io.Serializable;

/**
 * Перечисление возможных форм правления города.
 * Содержит четыре варианта: анархия, корпоратократия, ноократия и талассократия.
 * <p>
 * Реализует {@link Serializable} для передачи по сети между клиентом и сервером.
 * </p>
 *
 * @author Максим
 * @see Climate
 * @see StandardOfLiving
 */
public enum Government implements Serializable {
    ANARCHY,
    CORPORATOCRACY,
    NOOCRACY,
    THALASSOCRACY;

    /**
     * Преобразует строку в значение формы правления.
     * Поддерживает ввод в русской раскладке (автоматическая конвертация).
     *
     * @param value строковое представление формы правления
     * @return соответствующее значение Government или null
     */
    public static Government fromString(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }

        String input = value.trim().toUpperCase();

        try {
            return Government.valueOf(input);
        } catch (IllegalArgumentException e) {
            // Конвертировать раскладку)
            String converted = KeyboardLayoutConverter.convertRussianToEnglish(input);
            try {
                return Government.valueOf(converted);
            } catch (IllegalArgumentException e2) {
                return null;
            }
        }
    }

    /**
     * Возвращает форму правления по умолчанию (NOOCRACY).
     * Используется при некорректном вводе.
     *
     * @return значение по умолчанию
     */
    public static Government getDefault() {
        return NOOCRACY;
    }
}
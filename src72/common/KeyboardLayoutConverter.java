package common;

/**
 * Утилитный класс для конвертации текста с русской раскладки на английскую.
 * Полезен при вводе enum значений, когда пользователь забыл переключить раскладку.
 *
 * @author Максим
 */
public class KeyboardLayoutConverter {

    /**
     * Конвертирует текст с русской раскладки на английскую.
     *
     * @param input текст в русской раскладке
     * @return текст в английской раскладке
     */
    public static String convertRussianToEnglish(String input) {
        if (input == null || input.isEmpty()) {
            return input;
        }

        StringBuilder result = new StringBuilder();

        for (char ch : input.toCharArray()) {
            result.append(convertChar(ch));
        }

        return result.toString();
    }

    /**
     * Конвертирует один символ с русской раскладки на английскую.
     */
    private static char convertChar(char ch) {
        return switch (Character.toUpperCase(ch)) {
            case 'А' -> 'F';
            case 'Б' -> ',';
            case 'В' -> 'D';
            case 'Г' -> 'U';
            case 'Д' -> 'L';
            case 'Е' -> 'T';
            case 'Ё' -> '`';
            case 'Ж' -> ';';
            case 'З' -> 'P';
            case 'И' -> 'B';
            case 'Й' -> 'Q';
            case 'К' -> 'R';
            case 'Л' -> 'K';
            case 'М' -> 'V';
            case 'Н' -> 'Y';
            case 'О' -> 'J';
            case 'П' -> 'G';
            case 'Р' -> 'H';
            case 'С' -> 'C';
            case 'Т' -> 'N';
            case 'У' -> 'E';
            case 'Ф' -> 'A';
            case 'Х' -> '{';
            case 'Ц' -> 'W';
            case 'Ч' -> 'X';
            case 'Ш' -> 'I';
            case 'Щ' -> 'O';
            case 'Ъ' -> '}';
            case 'Ы' -> 'S';
            case 'Ь' -> 'M';
            case 'Э' -> '\'';
            case 'Ю' -> '.';
            case 'Я' -> 'Z';

            case 'а' -> 'f';
            case 'б' -> ',';
            case 'в' -> 'd';
            case 'г' -> 'u';
            case 'д' -> 'l';
            case 'е' -> 't';
            case 'ё' -> '`';
            case 'ж' -> ';';
            case 'з' -> 'p';
            case 'и' -> 'b';
            case 'й' -> 'q';
            case 'к' -> 'r';
            case 'л' -> 'k';
            case 'м' -> 'v';
            case 'н' -> 'y';
            case 'о' -> 'j';
            case 'п' -> 'g';
            case 'р' -> 'h';
            case 'с' -> 'c';
            case 'т' -> 'n';
            case 'у' -> 'e';
            case 'ф' -> 'a';
            case 'х' -> '{';
            case 'ц' -> 'w';
            case 'ч' -> 'x';
            case 'ш' -> 'i';
            case 'щ' -> 'o';
            case 'ъ' -> '}';
            case 'ы' -> 's';
            case 'ь' -> 'm';
            case 'э' -> '\'';
            case 'ю' -> '.';
            case 'я' -> 'z';

            // Если символ не из русской раскладки, возвращаем как есть
            default -> ch;
        };
    }

    /**
     * Проверяет, содержит ли текст символы русской раскладки.
     *
     * @param input текст для проверки
     * @return true если есть русские символы
     */
    public static boolean hasRussianLayout(String input) {
        if (input == null || input.isEmpty()) {
            return false;
        }

        for (char ch : input.toCharArray()) {
            if (ch >= 'А' && ch <= 'Я' || ch >= 'а' && ch <= 'я' || ch == 'Ё' || ch == 'ё') {
                return true;
            }
        }

        return false;
    }
}
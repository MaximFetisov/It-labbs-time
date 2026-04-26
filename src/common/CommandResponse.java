package common;

import common.City;
import java.io.Serializable;
import java.util.ArrayList;

/**
 * Объект ответа от сервера клиенту.
 * Содержит статус выполнения, сообщение и данные (список городов для show/info).
 * Все поля сериализуются для передачи по сети через UDP.
 *
 * @author Максим
 * @see Serializable
 * @see CommandRequest
 * @see City
 */
public class CommandResponse implements Serializable {
    private static final long serialVersionUID = 1L;

    private boolean success;
    private String message;
    private ArrayList<City> data;

    /**
     * Создаёт ответ без данных (для большинства команд).
     *
     * @param success true если команда выполнена успешно
     * @param message сообщение для пользователя
     */
    public CommandResponse(boolean success, String message) {
        this.success = success;
        this.message = message;
        this.data = null;
    }

    /**
     * Создаёт ответ с данными (для show, info, min_by_coordinates и т.д.).
     *
     * @param success true если команда выполнена успешно
     * @param message сообщение для пользователя
     * @param data список городов для отображения
     */
    public CommandResponse(boolean success, String message, ArrayList<City> data) {
        this.success = success;
        this.message = message;
        this.data = data;
    }

    /**
     * Проверяет успешность выполнения команды.
     *
     * @return true если команда выполнена успешно
     */
    public boolean isSuccess() {
        return success;
    }

    /**
     * Возвращает сообщение для пользователя.
     *
     * @return текст сообщения
     */
    public String getMessage() {
        return message;
    }

    /**
     * Возвращает данные (список городов).
     *
     * @return список городов или null
     */
    public ArrayList<City> getData() {
        return data;
    }

    /**
     * Проверяет, есть ли данные в ответе.
     *
     * @return true если данные присутствуют и не пустые
     */
    public boolean hasData() {
        return data != null && !data.isEmpty();
    }

    @Override
    public String toString() {
        return "CommandResponse{success=" + success +
                ", message='" + message + "', data=" +
                (data != null ? data.size() : 0) + "}";
    }
}
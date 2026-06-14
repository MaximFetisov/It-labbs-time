package common;

import common.City;
import java.io.Serializable;
import java.util.ArrayList;

/**
 * Объект ответа от сервера клиенту.
 * Содержит статус выполнения, сообщение, код ошибки и данные.
 *
 * @author Максим
 */
public class CommandResponse implements Serializable {
    private static final long serialVersionUID = 1L;

    private boolean success;
    private String message;
    private int errorCode;
    private ArrayList<City> data;

    // Конструктор без данных и без кода ошибки
    public CommandResponse(boolean success, String message) {
        this.success = success;
        this.message = message;
        this.errorCode = 0;
        this.data = null;
    }

    // Конструктор с данными
    public CommandResponse(boolean success, String message, ArrayList<City> data) {
        this.success = success;
        this.message = message;
        this.errorCode = 0;
        this.data = data;
    }

    // Новый конструктор с кодом ошибки
    public CommandResponse(boolean success, String message, int errorCode) {
        this.success = success;
        this.message = message;
        this.errorCode = errorCode;
        this.data = null;
    }

    // Конструктор со всеми параметрами
    public CommandResponse(boolean success, String message, int errorCode, ArrayList<City> data) {
        this.success = success;
        this.message = message;
        this.errorCode = errorCode;
        this.data = data;
    }

    public boolean isSuccess() {
        return success;
    }

    public String getMessage() {
        return message;
    }

    public ArrayList<City> getData() {
        return data;
    }

    public boolean hasData() {
        return data != null && !data.isEmpty();
    }

    // Геттер для кода ошибки
    public int getErrorCode() {
        return errorCode;
    }

    @Override
    public String toString() {
        return "CommandResponse{success=" + success +
                ", message='" + message + "', errorCode=" + errorCode +
                ", data=" + (data != null ? data.size() : 0) + "}";
    }
}
package servers.modules;

import common.CommandRequest;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.logging.Logger;

/**
 * Модуль чтения и десериализации запросов от клиентов.
 * Преобразует полученные байты в объект CommandRequest.
 *
 * @author Максим
 * @see CommandRequest
 */
public class RequestModule {
    private static final Logger logger = Logger.getLogger(RequestModule.class.getName());

    /**
     * Десериализует байты в объект CommandRequest.
     *
     * @param data байты, полученные от клиента
     * @return объект CommandRequest
     * @throws IOException если произошла ошибка десериализации
     * @throws ClassNotFoundException если класс не найден
     */
    public static CommandRequest deserialize(byte[] data) throws IOException, ClassNotFoundException {
        logger.info("Десериализация запроса...");
        
        try (ByteArrayInputStream bais = new ByteArrayInputStream(data);
             ObjectInputStream ois = new ObjectInputStream(bais)) {
            
            CommandRequest request = (CommandRequest) ois.readObject();
            logger.info("Запрос десериализован: " + request.getType());
            return request;
            
        } catch (IOException e) {
            logger.severe("Ошибка десериализации: " + e.getMessage());
            throw e;
        }
    }

    /**
     * Проверяет корректность запроса.
     *
     * @param request запрос для проверки
     * @return true если запрос валиден
     */
    public static boolean validate(CommandRequest request) {
        if (request == null) {
            logger.warning("Получен null запрос");
            return false;
        }
        
        if (request.getType() == null) {
            logger.warning("Тип команды не указан");
            return false;
        }
        
        logger.info("Запрос валиден: " + request.getType());
        return true;
    }
}
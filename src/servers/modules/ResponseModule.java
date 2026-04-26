package servers.modules;

import common.CommandResponse;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.logging.Logger;

/**
 * Модуль отправки ответов клиентам.
 * Сериализует объект CommandResponse в байты и отправляет через UDP.
 *
 * @author Максим
 * @see CommandResponse
 */
public class ResponseModule {
    private static final Logger logger = Logger.getLogger(ResponseModule.class.getName());

    /**
     * Сериализует объект CommandResponse в байтовый массив.
     *
     * @param response объект ответа
     * @return байтовый массив
     * @throws IOException если произошла ошибка сериализации
     */
    public static byte[] serialize(CommandResponse response) throws IOException {
        logger.info("Сериализация ответа...");
        
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
             ObjectOutputStream oos = new ObjectOutputStream(baos)) {
            
            oos.writeObject(response);
            oos.flush();
            byte[] data = baos.toByteArray();
            logger.info("Ответ сериализован: " + data.length + " байт");
            return data;
            
        } catch (IOException e) {
            logger.severe("Ошибка сериализации: " + e.getMessage());
            throw e;
        }
    }
}
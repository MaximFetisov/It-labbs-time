package servers.modules;

import common.CommandResponse;
import common.Chunk;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.logging.Logger;

public class ResponseModule {
    private static final Logger logger = Logger.getLogger(ResponseModule.class.getName());
    private static final int CHUNK_SIZE = 16000;
    private static final Random random = new Random();

    // Хранилище для отправки чанков (messageId -> список чанков)
    private static final java.util.Map<Integer, List<Chunk>> pendingMessages =
            new java.util.concurrent.ConcurrentHashMap<>();

    /**
     * Отправляет ответ клиенту с разбиением на чанки
     */
    public static void sendWithChunks(DatagramChannel channel,
                                      InetSocketAddress clientAddress,
                                      CommandResponse response) throws IOException {

        logger.info("Сериализация ответа...");
        byte[] responseData = serialize(response);
        logger.info("Ответ сериализован: " + responseData.length + " байт");

        // Разбиваем на чанки
        int messageId = random.nextInt();
        int totalChunks = (int) Math.ceil((double) responseData.length / CHUNK_SIZE);

        logger.info("Разбиение на " + totalChunks + " чанков...");

        List<Chunk> chunks = new ArrayList<>();

        for (int i = 0; i < totalChunks; i++) {
            int from = i * CHUNK_SIZE;
            int to = Math.min(from + CHUNK_SIZE, responseData.length);
            byte[] chunkData = new byte[to - from];
            System.arraycopy(responseData, from, chunkData, 0, chunkData.length);

            boolean isLast = (i == totalChunks - 1);
            Chunk chunk = new Chunk(messageId, i, totalChunks, chunkData, isLast);
            chunks.add(chunk);

            // Отправляем чанк
            byte[] chunkBytes = serializeChunk(chunk);
            channel.send(ByteBuffer.wrap(chunkBytes), clientAddress);

            // Небольшая пауза между чанками
            try {
                Thread.sleep(5);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        logger.info("Отправлено " + chunks.size() + " чанков клиенту " + clientAddress);
    }

    /**
     * Сериализует Chunk в байты.
     */
    private static byte[] serializeChunk(Chunk chunk) throws IOException {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
             ObjectOutputStream oos = new ObjectOutputStream(baos)) {
            oos.writeObject(chunk);
            oos.flush();
            return baos.toByteArray();
        }
    }

    /**
     * Сериализует CommandResponse в массив байтов.
     * Используется внутри sendWithChunks() для подготовки данных к разбиению.
     *
     * @param response объект для сериализации
     * @return массив байтов
     * @throws IOException если произошла ошибка сериализации
     */
    public static byte[] serialize(CommandResponse response) throws IOException {
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
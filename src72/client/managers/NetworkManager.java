package client.managers;

import common.Chunk;
import common.CommandResponse;
import common.CommandRequest;

import java.io.*;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class NetworkManager {
    private static final int bufferSize = 65535;
    private static final int RECEIVE_TIMEOUT = 10000;  // 10 секунд

    // Хранилище для получения чанков (messageId -> список чанков)
    private static final Map<Integer, List<Chunk>> receivedChunks =
            new ConcurrentHashMap<>();

    /**
     * Отправляет запрос и получает ответ
     */
    public CommandResponse sendRequest(DatagramSocket socket, CommandRequest request,
                                       String host, int port) {
        try {
            byte[] requestData = serialize(request);
            InetAddress address = InetAddress.getByName(host);

            DatagramPacket sendPacket = new DatagramPacket(
                    requestData,
                    requestData.length,
                    address,
                    port
            );
            socket.send(sendPacket);

            // Получаем ответ
            CommandResponse response = receiveWithChunks(socket);
            return response;

        } catch (IOException e) {
            System.err.println("Ошибка сетевого взаимодействия: " + e.getMessage());
            return null;
        } catch (ClassNotFoundException e) {
            System.err.println("Ошибка десериализации: " + e.getMessage());
            return null;
        }
    }

    /**
     * Получает ответ с поддержкой чанков.
     */
    private CommandResponse receiveWithChunks(DatagramSocket socket)
            throws IOException, ClassNotFoundException {

        socket.setSoTimeout(RECEIVE_TIMEOUT);

        List<Chunk> chunks = new ArrayList<>();
        Integer currentMessageId = null;
        int expectedTotalChunks = 0;
        long lastReceiveTime = System.currentTimeMillis();

        while (true) {
            byte[] receiveBuffer = new byte[bufferSize];
            DatagramPacket receivePacket = new DatagramPacket(receiveBuffer, bufferSize);

            try {
                socket.receive(receivePacket); //блок потока выполнения, пока не придёт пакет
                lastReceiveTime = System.currentTimeMillis();

                byte[] data = receivePacket.getData(); //пытается десериализовать их обратно в объект
                Object obj = deserialize(data);

                if (obj instanceof Chunk) {
                    Chunk chunk = (Chunk) obj;

                    // Новый messageId
                    if (currentMessageId == null) {
                        currentMessageId = chunk.getMessageId();
                        expectedTotalChunks = chunk.getTotalChunks();
                    }

                    // Проверяем, тот ли это messageId
                    if (chunk.getMessageId() != currentMessageId) {
                        continue;  // Пропускаем чанки от другого сообщения
                    }

                    chunks.add(chunk);

                    // Проверяем, все ли чанки получены
                    if (chunks.size() == expectedTotalChunks) {
                        // Собираем все чанки
                        CommandResponse response = assembleChunks(chunks);
                        receivedChunks.remove(currentMessageId);
                        return response;
                    }
                } else if (obj instanceof CommandResponse) {
                    // Ответ пришёл без разбиения на чанки (малевнький)
                    return (CommandResponse) obj;
                }

            } catch (java.net.SocketTimeoutException e) {
                //  Проверяем, не прошло ли слишком много времени
                if (System.currentTimeMillis() - lastReceiveTime > RECEIVE_TIMEOUT) {
                    System.err.println("Таймаут при получении чанков");
                    if (currentMessageId != null) {
                        receivedChunks.remove(currentMessageId);
                    }
                    return null;  // Возвращаем null вместо падения
                }
            }
        }
    }

    /**
     * Собирает CommandResponse из чанков.
     */
    private CommandResponse assembleChunks(List<Chunk> chunks) throws IOException, ClassNotFoundException {
        // Сортируем чанки по индексу
        chunks.sort((c1, c2) -> Integer.compare(c1.getChunkIndex(), c2.getChunkIndex()));

        // Объединяем данные
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        for (Chunk chunk : chunks) {
            baos.write(chunk.getData());
        }

        byte[] fullData = baos.toByteArray();

        // Десериализуем CommandResponse
        try (ByteArrayInputStream bais = new ByteArrayInputStream(fullData);
             ObjectInputStream ois = new ObjectInputStream(bais)) {
            return (CommandResponse) ois.readObject();
        }
    }

    /**
     * Сериализует объект в байты.
     */
    private byte[] serialize(Object obj) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (ObjectOutputStream oos = new ObjectOutputStream(baos)) {
            oos.writeObject(obj);
        }
        return baos.toByteArray();
    }

    /**
     * Десериализует байты в объект.
     */
    private Object deserialize(byte[] data) throws IOException, ClassNotFoundException {
        try (ByteArrayInputStream bais = new ByteArrayInputStream(data);
             ObjectInputStream ois = new ObjectInputStream(bais)) {
            return ois.readObject();
        }
    }
}
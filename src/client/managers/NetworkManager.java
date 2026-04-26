package client.managers;

import common.CommandRequest;
import common.CommandResponse;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

/**
 * Менеджер для сетевого взаимодействия с сервером.
 * Использует блокирующий DatagramSocket.
 *
 * @author Максим
 */
public class NetworkManager {
    private static final int bufferSize = 65535;

    /**
     * Отправляет запрос и получает ответ (блокирующий вызов).
     *
     * @param socket сокет для отправки
     * @param request запрос
     * @param host хост сервера
     * @param port порт сервера
     * @return ответ или null при ошибке
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

            byte[] receiveBuffer = new byte[bufferSize];
            DatagramPacket receivePacket = new DatagramPacket(receiveBuffer, bufferSize);
            socket.receive(receivePacket);

            CommandResponse response = (CommandResponse) deserialize(receivePacket.getData());
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
        ByteArrayInputStream bais = new ByteArrayInputStream(data);
        try (ObjectInputStream ois = new ObjectInputStream(bais)) {
            return ois.readObject();
        }
    }
}
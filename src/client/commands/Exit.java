package client.commands;

import client.managers.NetworkManager;
import common.CommandResponse;

import java.net.DatagramSocket;

/**
 * Команда для завершения работы клиентского приложения.
 * Корректно закрывает все ресурсы (сокет) и завершает выполнение клиента.
 * <p>
 * Команда выполняется локально на клиенте, запрос на сервер не отправляется.
 * Сервер продолжает работу после завершения клиента.
 * </p>
 *
 * @author Максим
 * @see DatagramSocket
 */
public class Exit {

    /**
     * Выполняет команду завершения работы клиента.
     *
     * @param socket сокет для закрытия
     * @param networkManager менеджер сети для закрытия ресурсов
     * @return результат выполнения команды
     */
    public CommandResponse execute(DatagramSocket socket, NetworkManager networkManager) {
        try {
            System.out.println("Завершение работы клиента...");

            if (socket != null && !socket.isClosed()) {
                socket.close();
                System.out.println("Сетевое соединение закрыто");
            }

            return new CommandResponse(true, "Клиент завершил работу");
        } catch (Exception e) {
            return new CommandResponse(false,
                    "Ошибка при завершении работы: " + e.getMessage());
        }
    }

    /**
     * Возвращает описание команды.
     *
     * @return описание команды
     */
    public String getDescription() {
        return "Завершить работу клиента (без сохранения в файл)";
    }

    /**
     * Возвращает имя команды.
     *
     * @return имя команды
     */
    public String getName() {
        return "exit";
    }
}
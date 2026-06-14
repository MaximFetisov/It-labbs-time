package client.commands;

import client.managers.NetworkManager;
import common.CommandResponse;

import java.awt.Desktop;
import java.net.DatagramSocket;
import java.net.URI;

/**
 * Клиентская команда-пасхалка (Never Gonna Give You Up).
 * Выполняется исключительно на стороне клиента.
 * Не отправляет запросы на сервер и не требует проверки авторизации.
 * Параметры login и password приняты для сохранения единой сигнатуры в dispatchCommand,
 * но внутри команды не используются.
 *
 * @author Максим
 */
public class Never {

    public CommandResponse execute(DatagramSocket socket,
                                   NetworkManager networkManager,
                                   String host,
                                   int port,
                                   String login,
                                   String password) {
        try {
            System.out.println("\n ╔═════════════════════════════════════════════════════╗");
            System.out.println(" ║                                                       ║");
            System.out.println(" ║   🎵  NEVER GONNA GIVE YOU UP  🎵                    ║");
            System.out.println(" ║                                                       ║");
            System.out.println(" ║   We're no strangers to love...                       ║");
            System.out.println(" ║   You know the rules and so do I...                   ║");
            System.out.println(" ║                                                       ║");
            System.out.println(" ╚═══════════════════════════════════════════════════════╝\n");

            String videoUrl = "https://www.youtube.com/watch?v=dQw4w9WgXcQ";

            if (Desktop.isDesktopSupported()) {
                Desktop desktop = Desktop.getDesktop();
                desktop.browse(new URI(videoUrl));
                System.out.println(" ▶ ️  Видео открыто в браузере!");
            } else {
                System.out.println(" ▶ ️  Откройте ссылку в браузере: " + videoUrl);
            }

            System.out.println("\n 🎵  You know the rules...  🎵 \n");
            return new CommandResponse(true, "Команда 'never' успешно выполнена (локально)");

        } catch (Exception e) {
            return new CommandResponse(false, "Ошибка при выполнении команды 'never': " + e.getMessage());
        }
    }
}
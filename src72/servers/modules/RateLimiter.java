package servers.modules;

import common.CommandType;

import java.net.InetAddress;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Менеджер ограничения частоты запросов (Rate Limiter).
 * Отслеживает количество запросов SHOW и ADD от каждого клиента (IP)
 * в рамках временного окна. При превышении лимита блокирует выполнение.
 */
public class RateLimiter {
    // Лимиты: максимум запросов за окно времени
    private static final int SHOW_MAX_REQUESTS = 5;
    private static final long SHOW_WINDOW_MS = 30_000; // 30 секунд

    private static final int ADD_MAX_REQUESTS = 3;
    private static final long ADD_WINDOW_MS = 30_000; // 30 секунд

    // Хранение состояния: "IP:CommandType" -> (начало окна, текущий счётчик)
    private final Map<String, WindowState> windows = new ConcurrentHashMap<>();

    private static class WindowState {
        long windowStart;
        int requestCount;

        WindowState(long windowStart) {
            this.windowStart = windowStart;
            this.requestCount = 1;
        }
    }

    /**
     * Проверяет, разрешён ли запрос для данного IP и типа команды.
     *
     * @param clientIp IP-адрес клиента
     * @param type     тип команды
     * @return true если запрос разрешён, false если лимит превышен
     */
    public synchronized boolean isAllowed(InetAddress clientIp, CommandType type) {
        if (type != CommandType.SHOW && type != CommandType.ADD) {
            return true;
        }

        int maxRequests = (type == CommandType.SHOW) ? SHOW_MAX_REQUESTS : ADD_MAX_REQUESTS;
        long windowMs = (type == CommandType.SHOW) ? SHOW_WINDOW_MS : ADD_WINDOW_MS;

        String key = clientIp.getHostAddress() + ":" + type.name();
        long now = System.currentTimeMillis();

        WindowState state = windows.get(key);

        // Если окна нет или оно истекло → создаём новое
        if (state == null || (now - state.windowStart) > windowMs) {
            windows.put(key, new WindowState(now));
            return true;
        }

        // Окно активно: проверяем счётчик
        if (state.requestCount < maxRequests) {
            state.requestCount++;
            return true;
        }

        return false;
    }

    /**
     * Сбрасывает все лимиты
     */
    public void reset() {
        windows.clear();
    }
}

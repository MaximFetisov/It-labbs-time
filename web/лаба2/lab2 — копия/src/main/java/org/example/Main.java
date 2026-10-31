package org.example;
// Попробовать документоориентиоованная база, как-то подключить. Файлы в формате csv возможность импорта этих файлов. В них точки. Выбор в какой файл записывать. Пока существует докер в нем своя файловая фс. пока файла нету они не сохраняются и писать нельзя. экспорт опционально
import com.fastcgi.FCGIInterface;

import java.net.URLDecoder;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Main {
    private static final List<String> HISTORY = new ArrayList<>();
    private static final double[] ALLOWED_R = {1, 1.5, 2, 2.5, 3};

    public static void main(String[] args) {
        while (new FCGIInterface().FCGIaccept() >= 0) {
            try {
                handleRequest();
            } catch (Exception e) {
                sendJson(500, "{\"error\":\"Internal server error\"}");
            }
        }
    }

    private static void handleRequest() {
        String rawUri = System.getProperty("REQUEST_URI", "/");
        String query = System.getProperty("QUERY_STRING", "");

        String path = rawUri.split("\\?")[0];
        Map<String, String> params = parseQuery(query);

        if (path.endsWith("/check")) {
            handleCheck(params);
        } else if (path.endsWith("/history")) {
            handleHistory();
        } else if (path.endsWith("/clear")) {
            handleClear();
        } else {
            sendJson(404, "{\"error\":\"Unknown endpoint\"}");
        }
    }

    private static void handleCheck(Map<String, String> params) {
        Double x = parseNum(params.get("x"));
        Double y = parseNum(params.get("y"));
        Double r = parseNum(params.get("r"));

        if (x == null || y == null || r == null) {
            sendJson(400, "{\"error\":\"Параметры x, y, r должны присутствовать и быть числами\"}");
            return;
        }
        if (x <= -5 || x >= 3) {
            sendJson(400, "{\"error\":\"X должен быть строго между -5 и 3\"}");
            return;
        }
        if (y < -4 || y > 4 || y != Math.rint(y)) {
            sendJson(400, "{\"error\":\"Y должен быть целым от -4 до 4\"}");
            return;
        }
        if (!isAllowedR(r)) {
            sendJson(400, "{\"error\":\"R должен быть одним из: 1, 1.5, 2, 2.5, 3\"}");
            return;
        }

        long start = System.nanoTime();
        boolean hit = checkHit(x, y, r);
        double execMs = (System.nanoTime() - start) / 1_000_000.0;

        String json = "{\"x\":" + num(x) + ",\"y\":" + num(y) + ",\"r\":" + num(r)
                + ",\"hit\":" + hit
                + ",\"checkTime\":\"" + Instant.now().toString() + "\""
                + ",\"executionTime\":" + num(Math.round(execMs * 1000) / 1000.0)
                + "}";


            HISTORY.add(json);

        sendJson(200, json);
    }

    private static void handleHistory() {
        StringBuilder sb = new StringBuilder("[");

            for (int i = 0; i < HISTORY.size(); i++) {
                if (i > 0) sb.append(',');
                sb.append(HISTORY.get(i));

        }
        sb.append(']');
        sendJson(200, sb.toString());
    }

    private static void handleClear() {

            HISTORY.clear();

        sendJson(200, "{\"status\":\"ok\"}");
    }

    private static boolean checkHit(double x, double y, double r) {
        if (x >= 0 && x <= r / 2 && y >= 0 && y <= r) return true;
        if (x <= 0 && y <= 0 && x * x + y * y <= r * r) return true;
        return x >= 0 && y <= 0 && y >= x - r / 2;
    }

    private static boolean isAllowedR(double r) {
        for (double allowed : ALLOWED_R) {
            if (Math.abs(r - allowed) < 1e-9) return true;
        }
        return false;
    }

    private static Double parseNum(String s) {
        if (s == null || s.trim().isEmpty()) return null;
        try {
            return Double.parseDouble(s.trim().replace(',', '.'));
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private static Map<String, String> parseQuery(String query) {
        Map<String, String> map = new HashMap<>();
        if (query == null || query.isEmpty()) return map;
        for (String pair : query.split("&")) {
            int idx = pair.indexOf('=');
            if (idx <= 0) continue;
            try {
                String key = URLDecoder.decode(pair.substring(0, idx), "UTF-8");
                String value = URLDecoder.decode(pair.substring(idx + 1), "UTF-8");
                map.put(key, value);
            } catch (Exception ignored) {
            }
        }
        return map;
    }

    private static String num(double v) {
        if (v == Math.rint(v) && !Double.isInfinite(v)) {
            return String.valueOf((long) v);
        }
        return String.valueOf(v);
    }

    private static void sendJson(int status, String body) {
        String reason = status == 200 ? "OK"
                : status == 400 ? "Bad Request"
                : status == 404 ? "Not Found"
                : "Internal Server Error";
        System.out.print("Status: " + status + " " + reason + "\n");
        System.out.print("Content-Type: application/json; charset=utf-8\n");
        System.out.print("\n");
        System.out.print(body);
        System.out.flush();
    }
}
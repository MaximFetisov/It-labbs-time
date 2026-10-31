package org.example;

import com.fastcgi.FCGIInterface;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStream;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

public class Main {
    // Папка с датасетами. Вне Docker можно переопределить: -DDATA_DIR=./data
    private static final Path DATA_DIR = Paths.get(System.getProperty("DATA_DIR", "/app/data"));
    private static final String CSV_HEADER = "x,y,r,hit,checkTime,executionTime";
    private static final double[] ALLOWED_R = {1, 1.5, 2, 2.5, 3};

    // Имя активного датасета (без .csv). null = ничего не выбрано => писать нельзя
    private static String activeDataset = null;

    public static void main(String[] args) {
        // Создаём папку, если её нет
        try {
            Files.createDirectories(DATA_DIR);
        } catch (Exception e) {
            System.err.println("Не удалось создать DATA_DIR: " + e.getMessage());
        }

        // одна итерация = один HTTP-запрос
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
        String method = System.getProperty("REQUEST_METHOD", "GET");
        String query = System.getProperty("QUERY_STRING", "");

        String path = rawUri.split("\\?")[0];
        Map<String, String> params = parseQuery(query);

        if (path.endsWith("/check")) {
            handleCheck(params);
        } else if (path.endsWith("/history")) {
            handleHistory();
        } else if (path.endsWith("/clear")) {
            handleClear();
        } else if (path.endsWith("/datasets")) {
            handleListDatasets();
        } else if (path.endsWith("/create")) {
            handleCreate(params);
        } else if (path.endsWith("/select")) {
            handleSelect(params);
        } else if (path.endsWith("/active")) {
            handleActive();
        } else if (path.endsWith("/import") && "POST".equalsIgnoreCase(method)) {
            handleImport(params);
        } else if (path.endsWith("/export")) {
            handleExport();
        } else {
            sendJson(404, "{\"error\":\"Unknown endpoint\"}");
        }
    }

    // Управление датасетами

    private static void handleActive() {
        if (activeDataset == null) {
            sendJson(200, "{\"dataset\":null}");
        } else {
            sendJson(200, "{\"dataset\":\"" + escape(activeDataset) + "\"}");
        }
    }

    private static void handleListDatasets() {
        try {
            List<String> names = new ArrayList<>();
            if (Files.exists(DATA_DIR)) {
                try (Stream<Path> s = Files.list(DATA_DIR)) {
                    s.filter(p -> p.toString().endsWith(".csv"))
                            .forEach(p -> names.add(p.getFileName().toString().replace(".csv", "")));
                }
            }
            StringBuilder sb = new StringBuilder("[");
            for (int i = 0; i < names.size(); i++) {
                if (i > 0) sb.append(',');
                sb.append('"').append(escape(names.get(i))).append('"');
            }
            sb.append(']');
            sendJson(200, sb.toString());
        } catch (Exception e) {
            sendJson(500, "{\"error\":\"list failed\"}");
        }
    }

    private static void handleCreate(Map<String, String> params) {
        String name = params.get("dataset");
        if (name == null || name.isEmpty()) {
            sendJson(400, "{\"error\":\"dataset name required\"}");
            return;
        }
        if (!name.matches("[A-Za-z0-9_\\-]+")) {
            sendJson(400, "{\"error\":\"invalid dataset name\"}");
            return;
        }

        Path file = DATA_DIR.resolve(name + ".csv");
        if (Files.exists(file)) {
            sendJson(409, "{\"error\":\"dataset already exists\"}");
            return;
        }
        try (BufferedWriter w = Files.newBufferedWriter(file, StandardCharsets.UTF_8)) {
            w.write(CSV_HEADER);
            w.newLine();
        } catch (Exception e) {
            sendJson(500, "{\"error\":\"create failed\"}");
            return;
        }
        activeDataset = name;
        sendJson(200, "{\"status\":\"ok\",\"dataset\":\"" + escape(name) + "\"}");
    }

    private static void handleSelect(Map<String, String> params) {
        String name = params.get("dataset");
        if (name == null || name.isEmpty()) {
            sendJson(400, "{\"error\":\"dataset name required\"}");
            return;
        }
        Path file = DATA_DIR.resolve(name + ".csv");
        if (!Files.exists(file)) {
            sendJson(404, "{\"error\":\"dataset not found\"}");
            return;
        }
        activeDataset = name;
        sendJson(200, "{\"status\":\"ok\",\"dataset\":\"" + escape(name) + "\"}");
    }

    //  Импорт CSV

    private static void handleImport(Map<String, String> params) {
        String mode = params.getOrDefault("mode", "new");   // "new" или "append"
        String name = params.get("dataset");                // имя датасета для режима new

        //  читаем тело запроса (содержимое CSV-файла)
        String body;
        try {
            int len = 0;
            try {
                len = Integer.parseInt(System.getProperty("CONTENT_LENGTH", "0"));
            } catch (NumberFormatException ignored) {
            }
            InputStream in = System.in;
            byte[] buf = new byte[Math.max(len, 0)];
            int total = 0, read;
            while (total < buf.length && (read = in.read(buf, total, buf.length - total)) > 0) {
                total += read;
            }
            body = new String(buf, 0, total, StandardCharsets.UTF_8);
        } catch (Exception e) {
            sendJson(500, "{\"error\":\"read body failed\"}");
            return;
        }

        // парсим строки (строго запятая-разделитель, минимум x,y,r)
        List<String> lines = new ArrayList<>();
        int skipped = 0;
        for (String raw : body.split("\\r?\\n")) {
            String line = raw.trim();
            if (line.isEmpty()) continue;
            if (line.toLowerCase().startsWith("x,")) continue;   // заголовок — не ошибка
            String[] p = line.split(",");
            if (p.length < 3) { skipped++; continue; }
            Double x = parseNum(p[0]);
            Double y = parseNum(p[1]);
            Double r = parseNum(p[2]);
            if (x == null || y == null || r == null) { skipped++; continue; }
            if (x <= -5 || x >= 3) { skipped++; continue; }
            if (y < -4 || y > 4 || y != Math.rint(y)) { skipped++; continue; }
            if (!isAllowedR(r)) { skipped++; continue; }
            boolean hit = checkHit(x, y, r);                     // hit пересчитываем сами
            lines.add(num(x) + "," + num(y) + "," + num(r) + "," + hit + ","
                    + Instant.now().toString() + ",0");
        }

        //  пишем в файл (новый датасет или дописываем в активный)
        if ("new".equals(mode)) {
            if (name == null || name.isEmpty() || !name.matches("[A-Za-z0-9_\\-]+")) {
                sendJson(400, "{\"error\":\"invalid dataset name\"}");
                return;
            }
            Path file = DATA_DIR.resolve(name + ".csv");
            if (Files.exists(file)) {
                sendJson(409, "{\"error\":\"dataset already exists\"}");
                return;
            }
            try (BufferedWriter w = Files.newBufferedWriter(file, StandardCharsets.UTF_8)) {
                w.write(CSV_HEADER);
                for (String l : lines) { w.newLine(); w.write(l); }
            } catch (Exception e) {
                sendJson(500, "{\"error\":\"write failed\"}");
                return;
            }
            activeDataset = name;
        } else {
            if (activeDataset == null) {
                sendJson(400, "{\"error\":\"Датасет не выбран\"}");
                return;
            }
            Path file = DATA_DIR.resolve(activeDataset + ".csv");
            try (BufferedWriter w = Files.newBufferedWriter(file, StandardCharsets.UTF_8, StandardOpenOption.APPEND)) {
                for (String l : lines) { w.newLine(); w.write(l); }
            } catch (Exception e) {
                sendJson(500, "{\"error\":\"write failed\"}");
                return;
            }
        }

        sendJson(200, "{\"imported\":" + lines.size() + ",\"skipped\":" + skipped + "}");
    }

    // Работа с точками
    private static void handleCheck(Map<String, String> params) {
        // Сначала проверяем, есть ли куда писать
        if (activeDataset == null) {
            sendJson(400, "{\"error\":\"Датасет не выбран. Создайте или выберите датасет.\"}");
            return;
        }

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
        double execRounded = Math.round(execMs * 1000) / 1000.0;

        // Время проверки — одно и то же для JSON и CSV
        String checkTime = Instant.now().toString();

        // JSON для ответа клиенту
        String json = "{\"x\":" + num(x) + ",\"y\":" + num(y) + ",\"r\":" + num(r)
                + ",\"hit\":" + hit
                + ",\"checkTime\":\"" + checkTime + "\""
                + ",\"executionTime\":" + num(execRounded)
                + "}";

        // CSV-строка для файла
        String csvLine = num(x) + "," + num(y) + "," + num(r) + "," + hit + ","
                + checkTime + "," + num(execRounded);

        // Дописываем строку в активный файл
        try (BufferedWriter w = Files.newBufferedWriter(
                DATA_DIR.resolve(activeDataset + ".csv"),
                StandardCharsets.UTF_8,
                StandardOpenOption.APPEND)) {
            w.newLine();
            w.write(csvLine);
        } catch (Exception e) {
            sendJson(500, "{\"error\":\"write failed: " + escape(e.getMessage()) + "\"}");
            return;
        }

        sendJson(200, json);
    }

    private static void handleHistory() {
        if (activeDataset == null) {
            sendJson(200, "[]");
            return;
        }
        Path file = DATA_DIR.resolve(activeDataset + ".csv");
        if (!Files.exists(file)) {
            sendJson(200, "[]");
            return;
        }
        List<String> rows = new ArrayList<>();
        try (BufferedReader r = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
            String header = r.readLine();  // пропускаем заголовок
            if (header == null) {
                sendJson(200, "[]");
                return;
            }
            String line;
            while ((line = r.readLine()) != null) {
                if (line.trim().isEmpty()) continue;
                String json = csvLineToJson(line);
                if (json != null) rows.add(json);
            }
        } catch (Exception e) {
            sendJson(500, "{\"error\":\"read failed\"}");
            return;
        }
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < rows.size(); i++) {
            if (i > 0) sb.append(',');
            sb.append(rows.get(i));
        }
        sb.append(']');
        sendJson(200, sb.toString());
    }

    private static void handleClear() {
        if (activeDataset == null) {
            sendJson(400, "{\"error\":\"Датасет не выбран\"}");
            return;
        }
        Path file = DATA_DIR.resolve(activeDataset + ".csv");
        // Открытие без APPEND = перезапись с нуля
        try (BufferedWriter w = Files.newBufferedWriter(file, StandardCharsets.UTF_8)) {
            w.write(CSV_HEADER);
            w.newLine();
        } catch (Exception e) {
            sendJson(500, "{\"error\":\"clear failed\"}");
            return;
        }
        sendJson(200, "{\"status\":\"ok\"}");
    }

    //  Экспорт (опционально)

    private static void handleExport() {
        if (activeDataset == null) {
            sendJson(400, "{\"error\":\"Датасет не выбран\"}");
            return;
        }
        Path file = DATA_DIR.resolve(activeDataset + ".csv");
        if (!Files.exists(file)) {
            sendJson(404, "{\"error\":\"dataset not found\"}");
            return;
        }
        try {
            String content = Files.readString(file, StandardCharsets.UTF_8);
            System.out.print("Status: 200 OK\r\n");
            System.out.print("Content-Type: text/csv; charset=utf-8\r\n");
            System.out.print("Content-Disposition: attachment; filename=\"" + activeDataset + ".csv\"\r\n");
            System.out.print("\r\n");
            System.out.print(content);
            System.out.flush();
        } catch (Exception e) {
            sendJson(500, "{\"error\":\"export failed\"}");
        }
    }

    //  Хелперы

    // Превращает строку CSV обратно в JSON (для handleHistory)
    private static String csvLineToJson(String line) {
        String[] p = line.split(",", -1);
        if (p.length < 6) return null;
        try {
            double x = Double.parseDouble(p[0]);
            double y = Double.parseDouble(p[1]);
            double r = Double.parseDouble(p[2]);
            boolean hit = Boolean.parseBoolean(p[3]);
            String checkTime = p[4];
            double exec = Double.parseDouble(p[5]);
            return "{\"x\":" + num(x) + ",\"y\":" + num(y) + ",\"r\":" + num(r)
                    + ",\"hit\":" + hit
                    + ",\"checkTime\":\"" + escape(checkTime) + "\""
                    + ",\"executionTime\":" + num(exec)
                    + "}";
        } catch (Exception e) {
            return null;
        }
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

    private static String escape(String s) {
        return s.replace("\\", "\\\\").replace("\"", "\\\"");
    }

    private static void sendJson(int status, String body) {
        String reason = status == 200 ? "OK"
                : status == 400 ? "Bad Request"
                : status == 404 ? "Not Found"
                : status == 409 ? "Conflict"
                : "Internal Server Error";
        System.out.print("Status: " + status + " " + reason + "\r\n");
        System.out.print("Content-Type: application/json; charset=utf-8\r\n");
        System.out.print("\r\n");
        System.out.print(body);
        System.out.flush();
    }
}

# ---- Переменные ----
SERVER   = python3
PORT     = 8000
SOURCES  = Index.html lab1.js
TARGET   = serve
HOST_URL = http://localhost:$(PORT)/Index.html

# ---- Default goal ----
all: $(TARGET)

# ---- Запуск локального HTTP-сервера ----
$(TARGET): $(SOURCES)
	@echo "Target:  $@"
	@echo "Sources: $^"
	@echo "Starting server on $(HOST_URL)"
	$(SERVER) -m http.server $(PORT)

# ---- Очистка временных файлов ----
clean:
	rm -f *.log
	rm -rf __pycache__

# ---- Просмотр текущих результатов ----
json:
	@cat lab1_results.json 2>/dev/null || echo "No results yet"

# ---- Phony targets ----
.PHONY: all $(TARGET) clean json

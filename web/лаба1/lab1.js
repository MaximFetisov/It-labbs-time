document.addEventListener('DOMContentLoaded', function() {

    const canvas = document.getElementById('graph');
    const ctx = canvas.getContext('2d');
    const MAX_R = 4;
    const PADDING = 40;

    const xButtons = document.querySelectorAll('.x-btn');
    const xValueInput = document.getElementById('x-value');
    const form = document.getElementById('check-form');
    const errorMessage = document.getElementById('error-message');
    const resultsBody = document.getElementById('results-body');
    const rInput = document.getElementById('r-input');
    const yInput = document.getElementById('y-input');
    const clearBtn = document.getElementById('clear-btn');

    const storageStatus = document.getElementById('storage-status');
    const connectBtn = document.getElementById('connect-file-btn');
    const createBtn = document.getElementById('create-file-btn');
    const exportBtn = document.getElementById('export-btn');
    const importBtn = document.getElementById('import-btn');
    const importInput = document.getElementById('import-input');

    const modalOverlay = document.getElementById('confirm-modal');
    const modalMessage = document.getElementById('modal-message');
    const modalCancel = document.getElementById('modal-cancel');
    const modalOk = document.getElementById('modal-ok');
    let modalCallback = null;

    let results = [];       // единственный источник данных во время сессии 
    let fileHandle = null;  // дескриптор подключённого файла на диске
    const supportsFS = ('showOpenFilePicker' in window); // проверка поддержки API

    // Обработка кнопок X
    xButtons.forEach(function(button) {
        button.addEventListener('click', function() {
            xButtons.forEach(function(btn) {
                btn.classList.remove('active');
            });
            this.classList.add('active');
            xValueInput.value = this.getAttribute('data-value');
        });
    });

    // Перерисовка при изменении R
    rInput.addEventListener('input', function() {
        const rStr = this.value.trim().replace(',', '.');
        const r = Number(rStr);
        if (!isNaN(r) && r > 1 && r < 4) {
            drawGraph(r);
        }
    });

    // Обработка отправки формы
    form.addEventListener('submit', function(event) {
        event.preventDefault();
        hideError();

        const xStr = xValueInput.value;
        const yStrRaw = yInput.value.trim();
        const rStrRaw = rInput.value.trim();

        if (xStr === '') {
            showError('Пожалуйста, выберите значение X из предложенных кнопок.');
            return;
        }
        const x = Number(xStr);

        if (yStrRaw === '') {
            showError('Поле Y не может быть пустым. Введите число.');
            return;
        }
        const yStr = yStrRaw.replace(',', '.');
        const y = Number(yStr);
        if (isNaN(y)) {
            showError('Координата Y должна быть числом (например, 1.5 или -2,5). Буквы и спецсимволы запрещены.');
            return;
        }
        if (y <= -3 || y >= 3) {
            showError('Координата Y должна быть строго между -3 и 3 (не включая границы). Введено: ' + y);
            return;
        }

        if (rStrRaw === '') {
            showError('Поле R не может быть пустым. Введите число.');
            return;
        }
        const rStr = rStrRaw.replace(',', '.');
        const r = Number(rStr);
        if (isNaN(r)) {
            showError('Радиус R должен быть числом (например, 2 или 2,5). Буквы и спецсимволы запрещены.');
            return;
        }
        if (r <= 1 || r >= 4) {
            showError('Радиус R должен быть строго между 1 и 4 (не включая границы). Введено: ' + r);
            return;
        }

        const isHit = checkHit(x, y, r);

        const now = new Date();
        const result = {
            x: x,
            y: y,
            r: r,
            time: now.toISOString(),
            timeDisplay: formatDateTime(now),
            hit: isHit
        };

        results.push(result);          // в память сессии
        addResultToTable(result);      // на экран
        persistResults();              // на диск (если файл подключён)

        drawGraph(r);
        drawPoint(x, y, r, isHit);

        form.reset();
        xValueInput.value = '';
        xButtons.forEach(function(btn) {
            btn.classList.remove('active');
        });
    });

    // Хранилище: File System Access API

    function updateStorageStatus() {
        if (fileHandle) {
            storageStatus.textContent = 'Хранилище: файл "' + fileHandle.name +
                '" на диске (вне кеша браузера).';
        } else if (supportsFS) {
            storageStatus.textContent = 'Хранилище не подключено: результаты живут только в оперативной памяти. Подключите или создайте файл.';
        } else {
            storageStatus.textContent = 'Браузер не поддерживает File System Access API: используйте экспорт и импорт JSON.';
        }
    }

    async function ensurePermission(handle) {
        const opts = { mode: 'readwrite' };
        if ((await handle.queryPermission(opts)) !== 'granted') {
            await handle.requestPermission(opts);
        }
    }

    // Подключить существующий файл
    async function connectFile() {
        try {
            const handles = await window.showOpenFilePicker({
                types: [{ description: 'JSON-файл', accept: { 'application/json': ['.json'] } }]
            });
            fileHandle = handles[0];
            await ensurePermission(fileHandle);
            await loadFromFile();
            updateStorageStatus();
        } catch (e) {
            if (e.name !== 'AbortError') {
                showError('Не удалось подключить файл: ' + e.message);
            }
        }
    }

    // Создать новый файл
    async function createFile() {
        try {
            fileHandle = await window.showSaveFilePicker({
                suggestedName: 'lab1_results.json',
                types: [{ description: 'JSON-файл', accept: { 'application/json': ['.json'] } }]
            });
            await ensurePermission(fileHandle);
            await persistResults();
            updateStorageStatus();
        } catch (e) {
            if (e.name !== 'AbortError') {
                showError('Не удалось создать файл: ' + e.message);
            }
        }
    }

    // Чтение данных из файла в память и на экран
    async function loadFromFile() {
        const file = await fileHandle.getFile();
        const text = await file.text();
        results = text.trim() ? JSON.parse(text) : [];
        rerenderTable();
    }

    // Запись всего массива результатов в файл
    async function persistResults() {
        if (!fileHandle) return;
        try {
            const writable = await fileHandle.createWritable();
            await writable.write(JSON.stringify(results, null, 2));
            await writable.close();
        } catch (e) {
            showError('Не удалось записать данные в файл: ' + e.message);
        }
    }

    // Перерисовка таблицы из массива results
    function rerenderTable() {
        resultsBody.innerHTML = '';
        results.forEach(function(result) {
            result.timeDisplay = formatDateTime(new Date(result.time));
            addResultToTable(result);
        });
    }

    connectBtn.addEventListener('click', connectFile);
    createBtn.addEventListener('click', createFile);

    // Запасной путь экспорт и импорт JSON 

    function exportResults() {
        const blob = new Blob([JSON.stringify(results, null, 2)], { type: 'application/json' });
        const a = document.createElement('a');
        a.href = URL.createObjectURL(blob);
        a.download = 'lab1_results.json';
        document.body.appendChild(a);
        a.click();
        a.remove();
        URL.revokeObjectURL(a.href);
    }

    exportBtn.addEventListener('click', exportResults);

    importBtn.addEventListener('click', function() {
        importInput.click();
    });

    importInput.addEventListener('change', function() {
        const file = this.files[0];
        if (!file) return;
        const reader = new FileReader();
        reader.onload = function() {
            try {
                results = JSON.parse(reader.result);
                rerenderTable();
            } catch (e) {
                showError('Выбранный файл содержит некорректный JSON');
            }
        };
        reader.readAsText(file);
        this.value = '';
    });

    // Скрытие основных кнопок и показ запасных, если API нет
    if (!supportsFS) {
        connectBtn.hidden = true;
        createBtn.hidden = true;
        exportBtn.hidden = false;
        importBtn.hidden = false;
    }
    updateStorageStatus();

    // Модальное окно

    function showModal(message, callback) {
        modalMessage.textContent = message;
        modalOverlay.classList.add('active');
        modalCallback = callback;
    }

    function closeModal() {
        modalOverlay.classList.remove('active');
        modalCallback = null;
    }

    clearBtn.addEventListener('click', function() {
        showModal('Вы уверены, что хотите удалить все результаты?', function() {
            results = [];
            rerenderTable();
            persistResults();   // очищаем и файл на диске
        });
    });

    modalCancel.addEventListener('click', closeModal);

    modalOk.addEventListener('click', function() {
        if (modalCallback) {
            modalCallback();
        }
        closeModal();
    });

    modalOverlay.addEventListener('click', function(event) {
        if (event.target === this) {
            closeModal();
        }
    });

    document.addEventListener('keydown', function(event) {
        if (event.key === 'Escape' && modalOverlay.classList.contains('active')) {
            closeModal();
        }
    });

    // Отрисовка графика 
        function drawGraph(r) {
        const width = canvas.width;
        const height = canvas.height;
        const centerX = width / 2;
        const centerY = height / 2;

        const availableSize = Math.min(width, height) - 2 * PADDING;
        const scale = availableSize / (2 * MAX_R);

        ctx.clearRect(0, 0, width, height);

        ctx.fillStyle = 'white';
        ctx.fillRect(0, 0, width, height);

        ctx.fillStyle = '#4da6ff';

        const rectLeft = centerX + (-r) * scale;
        const rectRight = centerX;
        const rectTop = centerY - (r/2) * scale;
        const rectBottom = centerY;

        ctx.beginPath();
        ctx.rect(rectLeft, rectTop, rectRight - rectLeft, rectBottom - rectTop);
        ctx.fill();

        const circleRadius = (r/2) * scale;
        ctx.beginPath();
        ctx.moveTo(centerX, centerY);
        ctx.arc(centerX, centerY, circleRadius, -Math.PI/2, 0);
        ctx.closePath();
        ctx.fill();

        const triLeft = centerX + (-r/2) * scale;
        const triBottom = centerY + (r/2) * scale;

        ctx.beginPath();
        ctx.moveTo(triLeft, centerY);
        ctx.lineTo(centerX, centerY);
        ctx.lineTo(centerX, triBottom);
        ctx.closePath();
        ctx.fill();

        ctx.strokeStyle = '#333';
        ctx.lineWidth = 2;

        ctx.beginPath();
        ctx.moveTo(PADDING, centerY);
        ctx.lineTo(width - PADDING, centerY);
        ctx.stroke();

        ctx.beginPath();
        ctx.moveTo(width - PADDING, centerY);
        ctx.lineTo(width - PADDING - 10, centerY - 5);
        ctx.lineTo(width - PADDING - 10, centerY + 5);
        ctx.closePath();
        ctx.fillStyle = '#333';
        ctx.fill();

        ctx.beginPath();
        ctx.moveTo(centerX, height - PADDING);
        ctx.lineTo(centerX, PADDING);
        ctx.stroke();

        ctx.beginPath();
        ctx.moveTo(centerX, PADDING);
        ctx.lineTo(centerX - 5, PADDING + 10);
        ctx.lineTo(centerX + 5, PADDING + 10);
        ctx.closePath();
        ctx.fill();

        ctx.font = 'bold 14px Arial';
        ctx.fillStyle = '#333';
        ctx.textAlign = 'left';
        ctx.fillText('X', width - PADDING + 10, centerY + 5);
        ctx.fillText('Y', centerX + 10, PADDING - 5);

        ctx.font = '12px Arial';
        ctx.fillStyle = '#333';

        const xMarks = [
            { val: -r, label: '-R' },
            { val: -r/2, label: '-R/2' },
            { val: r/2, label: 'R/2' },
            { val: r, label: 'R' }
        ];

        xMarks.forEach(function(m) {
            const px = centerX + m.val * scale;
            ctx.textAlign = 'center';
            ctx.beginPath();
            ctx.moveTo(px, centerY - 4);
            ctx.lineTo(px, centerY + 4);
            ctx.strokeStyle = '#333';
            ctx.lineWidth = 1;
            ctx.stroke();
            ctx.fillText(m.label, px, centerY + 18);
        });

        const yMarks = [
            { val: -r, label: '-R' },
            { val: -r/2, label: '-R/2' },
            { val: r/2, label: 'R/2' },
            { val: r, label: 'R' }
        ];

        yMarks.forEach(function(m) {
            const py = centerY - m.val * scale;
            ctx.textAlign = 'right';
            ctx.beginPath();
            ctx.moveTo(centerX - 4, py);
            ctx.lineTo(centerX + 4, py);
            ctx.strokeStyle = '#333';
            ctx.lineWidth = 1;
            ctx.stroke();
            ctx.fillText(m.label, centerX - 8, py + 4);
        });
    }

    // Отрисовка точки
    function drawPoint(x, y, r, isHit) {
        const width = canvas.width;
        const height = canvas.height;
        const centerX = width / 2;
        const centerY = height / 2;
        const scale = (Math.min(width, height) - 2 * PADDING) / (2 * MAX_R);

        const px = centerX + x * scale;
        const py = centerY - y * scale;

        ctx.beginPath();
        ctx.arc(px, py, 6, 0, 2 * Math.PI);
        ctx.fillStyle = isHit ? '#800020' : '#666';
        ctx.fill();
        ctx.strokeStyle = 'white';
        ctx.lineWidth = 2;
        ctx.stroke();
    }

    // Проверка попадания 
    function checkHit(x, y, r) {
        if (x >= -r && x <= 0 && y >= 0 && y <= r/2) {
            return true;
        }
        if (x >= 0 && y >= 0 && (x*x + y*y) <= (r/2)*(r/2)) {
            return true;
        }
        if (x >= -r/2 && x <= 0 && y >= -r/2 && y <= 0 && y >= -x - r/2) {
            return true;
        }
        return false;
    }

    // Форматирование даты 
    function formatDateTime(date) {
        const options = {
            year: 'numeric',
            month: 'long',
            day: 'numeric',
            hour: '2-digit',
            minute: '2-digit',
            second: '2-digit'
        };
        return new Intl.DateTimeFormat('ru-RU', options).format(date);
    }

    // Таблица
    function addResultToTable(result) {
        const row = document.createElement('tr');

        const cells = [
            result.x,
            result.y,
            result.r,
            result.timeDisplay,
            result.hit ? 'Попала' : 'Не попала'
        ];

        cells.forEach(function(text, index) {
            const td = document.createElement('td');
            td.textContent = text;
            if (index === 4) {
                td.style.color = result.hit ? '#800020' : '#666';
                td.style.fontWeight = 'bold';
            }
            row.appendChild(td);
        });

        resultsBody.appendChild(row);
    }

    // Ошибки
    function showError(message) {
        errorMessage.textContent = message;
        errorMessage.style.display = 'block';
        setTimeout(hideError, 4000);
    }

    function hideError() {
        errorMessage.style.display = 'none';
        errorMessage.textContent = '';
    }

    // Инициализация
    drawGraph(2);
});
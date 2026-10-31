document.addEventListener('DOMContentLoaded', function() {
    // Canvas
    const canvas = document.getElementById('graph');
    const ctx = canvas.getContext('2d');
    const PADDING = 40;

    // DOM
    const form = document.getElementById('check-form');
    const xInput = document.getElementById('x-input');
    const ySelect = document.getElementById('y-select');
    const rCheckboxes = document.querySelectorAll('input[name="r"]');
    const errorMessage = document.getElementById('error-message');
    const resultsBody = document.getElementById('results-body');
    const clearBtn = document.getElementById('clear-btn');

    const modalOverlay = document.getElementById('confirm-modal');
    const modalMessage = document.getElementById('modal-message');
    const modalCancel = document.getElementById('modal-cancel');
    const modalOk = document.getElementById('modal-ok');
    let modalCallback = null;

    // Датасеты
    const datasetSelect = document.getElementById('dataset-select');
    const datasetRefresh = document.getElementById('dataset-refresh');
    const datasetCreateBtn = document.getElementById('dataset-create');
    const datasetNameInput = document.getElementById('dataset-name');
    const datasetStatus = document.getElementById('dataset-status');
    const importFile = document.getElementById('import-file');
    const importBtn = document.getElementById('import-btn');
    const importReport = document.getElementById('import-report');
    const exportBtn = document.getElementById('export-btn');
    let activeDataset = null;

    // Контракт с сервером
    const API = {
        check: 'calculate/check',
        history: 'calculate/history',
        clear: 'calculate/clear',
        datasets: 'calculate/datasets',
        create: 'calculate/create',
        select: 'calculate/select',
        active: 'calculate/active',
        import: 'calculate/import',
        export: 'calculate/export'
    };

    // Чекбоксы R (взаимоисключение)
    rCheckboxes.forEach(function(cb) {
        cb.addEventListener('change', function() {
            if (this.checked) {
                rCheckboxes.forEach(function(other) {
                    if (other !== cb) other.checked = false;
                });
            }
            drawGraph(getSelectedR() || 2);
        });
    });

    function getSelectedR() {
        let value = null;
        rCheckboxes.forEach(function(c) {
            if (c.checked) value = Number(c.value);
        });
        return value;
    }

    // Отправка формы
    form.addEventListener('submit', function(event) {
        event.preventDefault();
        hideError();

        if (!activeDataset) {
            showError('Сначала выберите или создайте датасет.');
            return;
        }

        const xStr = xInput.value.trim().replace(',', '.');
        if (xStr === '') {
            showError('Поле X не может быть пустым. Введите число строго от -5 до 3.');
            return;
        }
        const x = Number(xStr);
        if (isNaN(x)) {
            showError('Координата X должна быть числом (например, -2.5 или 1,5). Буквы запрещены.');
            return;
        }
        if (x <= -5 || x >= 3) {
            showError('Координата X должна быть строго между -5 и 3 (не включая границы). Введено: ' + x);
            return;
        }

        if (ySelect.value === '') {
            showError('Пожалуйста, выберите значение Y из списка.');
            return;
        }
        const y = Number(ySelect.value);

        const r = getSelectedR();
        if (r === null) {
            showError('Пожалуйста, отметьте радиус R.');
            return;
        }

        sendCheck(x, y, r);
        form.reset();
    });

    // Очистка истории
    clearBtn.addEventListener('click', function() {
        if (!activeDataset) {
            showError('Датасет не выбран — очищать нечего.');
            return;
        }
        showModal('Вы уверены, что хотите удалить все результаты?', function() {
            fetch(API.clear)
                .then(function(response) {
                    if (!response.ok) throw new Error('status ' + response.status);
                    resultsBody.innerHTML = '';
                })
                .catch(function(e) {
                    showError('Не удалось очистить историю: ' + e.message);
                });
        });
    });

    // Проверка одной точки
    async function sendCheck(x, y, r) {
        const params = new URLSearchParams({ x: x, y: y, r: r });
        try {
            const response = await fetch(API.check + '?' + params.toString());
            if (!response.ok) {
                let message = 'Сервер вернул ошибку ' + response.status;
                try {
                    const err = await response.json();
                    if (err.error) message = err.error;
                } catch (ignored) {}
                showError(message);
                return;
            }
            const result = await response.json();
            addResultToTable(result);
            selectRow(resultsBody.lastElementChild);
            drawGraph(result.r);
            drawPoint(result.x, result.y, result.r, result.hit);
        } catch (e) {
            showError('Не удалось связаться с сервером: ' + e.message);
        }
    }

    // Загрузка истории
    async function loadHistory() {
        resultsBody.innerHTML = '';
        try {
            const response = await fetch(API.history);
            if (!response.ok) return;
            const results = await response.json();
            results.forEach(function(result) {
                addResultToTable(result);
                drawPoint(result.x, result.y, result.r, result.hit);
            });
        } catch (e) {
            console.warn('История недоступна:', e.message);
        }
    }

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

    modalCancel.addEventListener('click', closeModal);
    modalOk.addEventListener('click', function() {
        if (modalCallback) modalCallback();
        closeModal();
    });
    modalOverlay.addEventListener('click', function(event) {
        if (event.target === this) closeModal();
    });
    document.addEventListener('keydown', function(event) {
        if (event.key === 'Escape' && modalOverlay.classList.contains('active')) closeModal();
    });

    // Масштаб и система координат
    const X_MIN = -5, X_MAX = 3;
    const Y_MIN = -4, Y_MAX = 4;

    function scale() {
        return (Math.min(canvas.width, canvas.height) - 2 * PADDING) /
               Math.max(X_MAX - X_MIN, Y_MAX - Y_MIN);
    }

    function toPx(x, y) {
        const s = scale();
        const ox = PADDING + (0 - X_MIN) * s;
        const oy = PADDING + (Y_MAX - 0) * s;
        return { px: ox + x * s, py: oy - y * s };
    }

    // Отрисовка области варианта 792
    function drawGraph(r) {
        const width = canvas.width;
        const height = canvas.height;
        const s = scale();
        const o = toPx(0, 0);

        ctx.clearRect(0, 0, width, height);
        ctx.fillStyle = 'white';
        ctx.fillRect(0, 0, width, height);

        ctx.fillStyle = '#FFCCCB';

        // Прямоугольник
        ctx.beginPath();
        ctx.rect(o.px, o.py - r * s, (r / 2) * s, r * s);
        ctx.fill();

        // Четверть круга
        ctx.beginPath();
        ctx.moveTo(o.px, o.py);
        ctx.arc(o.px, o.py, r * s, Math.PI / 2, Math.PI);
        ctx.closePath();
        ctx.fill();

        // Треугольник
        ctx.beginPath();
        ctx.moveTo(o.px, o.py);
        ctx.lineTo(o.px + (r / 2) * s, o.py);
        ctx.lineTo(o.px, o.py + (r / 2) * s);
        ctx.closePath();
        ctx.fill();

        // Оси
        ctx.strokeStyle = '#333';
        ctx.lineWidth = 2;

        const xL = toPx(X_MIN, 0), xR = toPx(X_MAX, 0);
        ctx.beginPath();
        ctx.moveTo(xL.px, o.py);
        ctx.lineTo(xR.px, o.py);
        ctx.stroke();
        ctx.beginPath();
        ctx.moveTo(xR.px, o.py);
        ctx.lineTo(xR.px - 10, o.py - 5);
        ctx.lineTo(xR.px - 10, o.py + 5);
        ctx.closePath();
        ctx.fillStyle = '#333';
        ctx.fill();

        const yB = toPx(0, Y_MIN), yT = toPx(0, Y_MAX);
        ctx.beginPath();
        ctx.moveTo(o.px, yB.py);
        ctx.lineTo(o.px, yT.py);
        ctx.stroke();
        ctx.beginPath();
        ctx.moveTo(o.px, yT.py);
        ctx.lineTo(o.px - 5, yT.py + 10);
        ctx.lineTo(o.px + 5, yT.py + 10);
        ctx.closePath();
        ctx.fill();

        ctx.font = 'bold 14px Arial';
        ctx.fillStyle = '#333';
        ctx.textAlign = 'left';
        ctx.fillText('X', xR.px + 10, o.py + 5);
        ctx.fillText('Y', o.px + 10, yT.py - 5);

        // Метки
        ctx.font = '12px Arial';
        const marks = [
            { val: -r, label: '-R' },
            { val: -r / 2, label: '-R/2' },
            { val: r / 2, label: 'R/2' },
            { val: r, label: 'R' }
        ];

        marks.forEach(function(m) {
            const p = toPx(m.val, 0);
            ctx.textAlign = 'center';
            ctx.beginPath();
            ctx.moveTo(p.px, o.py - 4);
            ctx.lineTo(p.px, o.py + 4);
            ctx.strokeStyle = '#333';
            ctx.lineWidth = 1;
            ctx.stroke();
            ctx.fillText(m.label, p.px, o.py + 18);
        });

        marks.forEach(function(m) {
            const p = toPx(0, m.val);
            ctx.textAlign = 'right';
            ctx.beginPath();
            ctx.moveTo(o.px - 4, p.py);
            ctx.lineTo(o.px + 4, p.py);
            ctx.strokeStyle = '#333';
            ctx.lineWidth = 1;
            ctx.stroke();
            ctx.fillText(m.label, o.px - 8, p.py + 4);
        });
    }

    // Точка результата
    function drawPoint(x, y, r, isHit) {
        const p = toPx(x, y);
        ctx.beginPath();
        ctx.arc(p.px, p.py, 6, 0, 2 * Math.PI);
        ctx.fillStyle = isHit ? '#800020' : '#666';
        ctx.fill();
        ctx.strokeStyle = 'white';
        ctx.lineWidth = 2;
        ctx.stroke();
    }

    // Таблица
    function addResultToTable(result) {
        const row = document.createElement('tr');
        const cells = [
            result.x,
            result.y,
            result.r,
            formatDateTime(new Date(result.checkTime)),
            Number(result.executionTime).toFixed(2) + ' мс',
            result.hit ? 'Попала' : 'Не попала'
        ];

        cells.forEach(function(text, index) {
            const td = document.createElement('td');
            td.textContent = text;
            if (index === 5) {
                td.style.color = result.hit ? '#800020' : '#666';
                td.style.fontWeight = 'bold';
            }
            row.appendChild(td);
        });

        row.title = 'Нажмите, чтобы показать точку на графике';
        row.addEventListener('click', function() {
            selectRow(row);
            drawGraph(result.r);
            drawPoint(result.x, result.y, result.r, result.hit);
        });

        resultsBody.appendChild(row);
    }

    function selectRow(row) {
        resultsBody.querySelectorAll('tr.selected').forEach(function(r) {
            r.classList.remove('selected');
        });
        row.classList.add('selected');
    }

    // Формат даты
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

    //  Датасеты
    async function loadDatasets() {
        try {
            const response = await fetch(API.datasets);
            if (!response.ok) return;
            const names = await response.json();
            datasetSelect.innerHTML = '';
            const placeholder = document.createElement('option');
            placeholder.value = '';
            placeholder.disabled = true;
            placeholder.selected = true;
            placeholder.textContent = '— не выбран —';
            datasetSelect.appendChild(placeholder);
            names.forEach(function(name) {
                const opt = document.createElement('option');
                opt.value = name;
                opt.textContent = name;
                datasetSelect.appendChild(opt);
            });
            await loadActive();
        } catch (e) {
            console.warn('Список датасетов недоступен:', e.message);
        }
    }

    async function loadActive() {
        try {
            const response = await fetch(API.active);
            if (!response.ok) return;
            const data = await response.json();
            setActive(data.dataset);
            datasetSelect.value = activeDataset || '';
        } catch (e) {
            console.warn('Активный датасет неизвестен:', e.message);
        }
    }

    function setActive(name) {
        activeDataset = name;
        datasetStatus.textContent = name ? ('активный: ' + name) : 'датасет не выбран';
    }

    datasetSelect.addEventListener('change', async function() {
        const name = datasetSelect.value;
        if (!name) return;
        try {
            const response = await fetch(API.select + '?dataset=' + encodeURIComponent(name));
            if (!response.ok) {
                const err = await response.json().catch(function() { return {}; });
                showError(err.error || ('Сервер вернул ошибку ' + response.status));
                return;
            }
            setActive(name);
            loadHistory();
        } catch (e) {
            showError('Не удалось выбрать датасет: ' + e.message);
        }
    });

    datasetRefresh.addEventListener('click', loadDatasets);

    datasetCreateBtn.addEventListener('click', async function() {
        const name = datasetNameInput.value.trim();
        if (name === '') {
            showError('Введите имя нового датасета.');
            return;
        }
        try {
            const response = await fetch(API.create + '?dataset=' + encodeURIComponent(name));
            const data = await response.json().catch(function() { return {}; });
            if (!response.ok) {
                showError(data.error || ('Сервер вернул ошибку ' + response.status));
                return;
            }
            datasetNameInput.value = '';
            await loadDatasets();
            setActive(name);
            datasetSelect.value = name;
            loadHistory();
        } catch (e) {
            showError('Не удалось создать датасет: ' + e.message);
        }
    });

    //  Импорт CSV
    importBtn.addEventListener('click', async function() {
        const file = importFile.files[0];
        if (!file) {
            showError('Выберите CSV-файл для импорта.');
            return;
        }
        const mode = document.querySelector('input[name="import-mode"]:checked').value;
        if (mode === 'append' && !activeDataset) {
            showError('Датасет не выбран — дописывать некуда.');
            return;
        }
        const name = file.name.replace(/\.csv$/i, '').replace(/[^A-Za-z0-9_\-]/g, '_');
        try {
            const text = await file.text();
            const response = await fetch(API.import + '?mode=' + mode + '&dataset=' + encodeURIComponent(name), {
                method: 'POST',
                body: text,
                headers: { 'Content-Type': 'text/plain; charset=utf-8' }
            });
            const data = await response.json().catch(function() { return {}; });
            if (!response.ok) {
                showError(data.error || ('Сервер вернул ошибку ' + response.status));
                return;
            }
            importReport.textContent = 'принято: ' + data.imported + ', пропущено: ' + data.skipped;
            importFile.value = '';
            await loadDatasets();
            if (mode === 'new') {
                setActive(name);
                datasetSelect.value = name;
            }
            loadHistory();
        } catch (e) {
            showError('Не удалось импортировать: ' + e.message);
        }
    });

        exportBtn.addEventListener('click', function() {
            if (!activeDataset) {
                showError('Датасет не выбран — скачивать нечего.');
                return;
            }
            const a = document.createElement('a');
            a.href = API.export;
            document.body.appendChild(a);
            a.click();
            a.remove();
        });

    // Инициализация
    drawGraph(2);
    loadDatasets();
});
document.addEventListener('DOMContentLoaded', function() {

    // === Canvas ===
    const canvas = document.getElementById('graph');
    const ctx = canvas.getContext('2d');
    const MAX_R = 3;
    const PADDING = 40;

    // === DOM ===
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

    // === Данные сессии (локально, до появления сервера) ===
    let results = [];

    // === Чекбоксы: один выбор + перерисовка ===
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

    // === Отправка формы ===
    form.addEventListener('submit', function(event) {
        event.preventDefault();
        hideError();

        // --- X ---
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

        // --- Y ---
        if (ySelect.value === '') {
            showError('Пожалуйста, выберите значение Y из списка.');
            return;
        }
        const y = Number(ySelect.value);

        // --- R ---
        const r = getSelectedR();
        if (r === null) {
            showError('Пожалуйста, отметьте радиус R.');
            return;
        }

        // --- Проверка с замером времени работы ---
        const start = performance.now();
        const isHit = checkHit(x, y, r);
        const executionTime = performance.now() - start;

        const result = {
            x: x,
            y: y,
            r: r,
            checkTime: new Date().toISOString(),
            executionTime: executionTime,
            hit: isHit
        };

        results.push(result);
        addResultToTable(result);
        drawGraph(r);
        drawPoint(x, y, r, isHit);

        form.reset();
    });

    // === Очистка таблицы ===
    clearBtn.addEventListener('click', function() {
        showModal('Вы уверены, что хотите удалить все результаты?', function() {
            results = [];
            resultsBody.innerHTML = '';
        });
    });

    // === Модальное окно ===
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

    // === Проверка попадания: область варианта 792 ===
    function checkHit(x, y, r) {
        // 1. Прямоугольник: 0 <= x <= R/2, 0 <= y <= R
        if (x >= 0 && x <= r/2 && y >= 0 && y <= r) return true;
        // 2. Четверть круга: x <= 0, y <= 0, x^2 + y^2 <= R^2
        if (x <= 0 && y <= 0 && x*x + y*y <= r*r) return true;
        // 3. Треугольник: x >= 0, y <= 0, y >= x - R/2
        if (x >= 0 && y <= 0 && y >= x - r/2) return true;
        return false;
    }

    // === Отрисовка области ===
    function drawGraph(r) {
        const width = canvas.width;
        const height = canvas.height;
        const centerX = width / 2;
        const centerY = height / 2;
        const scale = (Math.min(width, height) - 2 * PADDING) / (2 * MAX_R);

        ctx.clearRect(0, 0, width, height);
        ctx.fillStyle = 'white';
        ctx.fillRect(0, 0, width, height);

        ctx.fillStyle = '#FFCCCB ';

        // Прямоугольник: x ∈ [0, R/2], y ∈ [0, R]
        ctx.beginPath();
        ctx.rect(centerX, centerY - r * scale, (r/2) * scale, r * scale);
        ctx.fill();

        // Четверть круга: третья четверть, радиус R
        ctx.beginPath();
        ctx.moveTo(centerX, centerY);
        ctx.arc(centerX, centerY, r * scale, Math.PI / 2, Math.PI);
        ctx.closePath();
        ctx.fill();

        // Треугольник: (0,0), (R/2, 0), (0, -R/2)
        ctx.beginPath();
        ctx.moveTo(centerX, centerY);
        ctx.lineTo(centerX + (r/2) * scale, centerY);
        ctx.lineTo(centerX, centerY + (r/2) * scale);
        ctx.closePath();
        ctx.fill();

        // === Оси ===
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

        // === Метки ===
        ctx.font = '12px Arial';
        const marks = [
            { val: -r, label: '-R' },
            { val: -r/2, label: '-R/2' },
            { val: r/2, label: 'R/2' },
            { val: r, label: 'R' }
        ];

        marks.forEach(function(m) {
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

        marks.forEach(function(m) {
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

    // === Точка результата ===
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

    // === Таблица: 6 колонок ===
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

        resultsBody.appendChild(row);
    }

    // === Формат даты ===
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

    // === Ошибки ===
    function showError(message) {
        errorMessage.textContent = message;
        errorMessage.style.display = 'block';
        setTimeout(hideError, 4000);
    }

    function hideError() {
        errorMessage.style.display = 'none';
        errorMessage.textContent = '';
    }

    // === Инициализация ===
    drawGraph(2);
});
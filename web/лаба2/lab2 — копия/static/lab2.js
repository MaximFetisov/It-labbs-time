document.addEventListener('DOMContentLoaded', function() {

    //   Canvas
    const canvas = document.getElementById('graph');
    const ctx = canvas.getContext('2d');
    const MAX_R = 3;
    const PADDING = 40;

    //   DOM
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

    //   Контракт с сервером
    const API = {
        check: 'calculate/check',
        history: 'calculate/history',
        clear: 'calculate/clear'
    };

    //  Чекбоксы
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

    //   Отправка формы
    form.addEventListener('submit', function(event) {
        event.preventDefault();
        hideError();

        // X
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

        // Y
        if (ySelect.value === '') {
            showError('Пожалуйста, выберите значение Y из списка.');
            return;
        }
        const y = Number(ySelect.value);

        // R
        const r = getSelectedR();
        if (r === null) {
            showError('Пожалуйста, отметьте радиус R.');
            return;
        }

        sendCheck(x, y, r);
        form.reset();
    });

    //   Очистка истории
    clearBtn.addEventListener('click', function() {
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

    //   проверка одной точки
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

    //   загрузка истории при старте
    async function loadHistory() {
        try {
            const response = await fetch(API.history);
            if (!response.ok) return;
            const results = await response.json();
            results.forEach(function(result) {
                addResultToTable(result);
            });
        } catch (e) {
            console.warn('История недоступна:', e.message);
        }
    }

    //   Модальное окно
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

   // Диапазон отображения = полный диапазон валидных вводов
       const X_MIN = -5, X_MAX = 3;
       const Y_MIN = -4, Y_MAX = 4;

       function scale() {
           return (Math.min(canvas.width, canvas.height) - 2 * PADDING) /
                  Math.max(X_MAX - X_MIN, Y_MAX - Y_MIN);
       }

       /** Математические координаты -> пиксели */
       function toPx(x, y) {
           const s = scale();
           const ox = PADDING + (0 - X_MIN) * s;   // пиксель, где x = 0
           const oy = PADDING + (Y_MAX - 0) * s;   // пиксель, где y = 0
           return { px: ox + x * s, py: oy - y * s };
       }

       //   Отрисовка области варианта 792
       function drawGraph(r) {
           const width = canvas.width;
           const height = canvas.height;
           const s = scale();
           const o = toPx(0, 0);

           ctx.clearRect(0, 0, width, height);
           ctx.fillStyle = 'white';
           ctx.fillRect(0, 0, width, height);

           ctx.fillStyle = '#FFCCCB';

           // Прямоугольник: x ∈ [0, R/2], y ∈ [0, R]
           ctx.beginPath();
           ctx.rect(o.px, o.py - r * s, (r / 2) * s, r * s);
           ctx.fill();

           // Четверть круга: третья четверть
           ctx.beginPath();
           ctx.moveTo(o.px, o.py);
           ctx.arc(o.px, o.py, r * s, Math.PI / 2, Math.PI);
           ctx.closePath();
           ctx.fill();

           // Треугольник: (0,0), (R/2, 0), (0, -R/2)
           ctx.beginPath();
           ctx.moveTo(o.px, o.py);
           ctx.lineTo(o.px + (r / 2) * s, o.py);
           ctx.lineTo(o.px, o.py + (r / 2) * s);
           ctx.closePath();
           ctx.fill();

           // Оси на весь диапазон вводов
           ctx.strokeStyle = '#333';
           ctx.lineWidth = 2;

           const xL = toPx(X_MIN, 0), xR = toPx(X_MAX, 0);
           ctx.beginPath();
           ctx.moveTo(xL.px, o.py);
           ctx.lineTo(xR.px, o.py);
           ctx.stroke();
           ctx.beginPath();                       // стрелка X
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
           ctx.beginPath();                       // стрелка Y
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

           // Метки -R, -R/2, R/2, R
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

       //   Точка результата
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

    //   Таблица
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

            // клик по строке показывает точку на графике
            row.title = 'Нажмите, чтобы показать точку на графике';
            row.addEventListener('click', function() {
                selectRow(row);
                drawGraph(result.r);                              // область под R этой строки
                drawPoint(result.x, result.y, result.r, result.hit);  // сама точка
            });

            resultsBody.appendChild(row);
        }

        //  подсветка выбранной строки
        function selectRow(row) {
            resultsBody.querySelectorAll('tr.selected').forEach(function(r) {
                r.classList.remove('selected');
            });
            row.classList.add('selected');
        }

    //   Формат даты
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

    //   Ошибки
    function showError(message) {
        errorMessage.textContent = message;
        errorMessage.style.display = 'block';
        setTimeout(hideError, 4000);
    }

    function hideError() {
        errorMessage.style.display = 'none';
        errorMessage.textContent = '';
    }

    //   Инициализация
    drawGraph(2);
    loadHistory();
});
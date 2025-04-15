<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>Игровой режим – Собери пазл</title>

    <!-- Задаём размер поля в зависимости от сложности -->
    <c:choose>
        <c:when test="${gameSession.puzzle.difficulty == 'EASY'}">
            <c:set var="gridSize" value="4"/>
        </c:when>
        <c:when test="${gameSession.puzzle.difficulty == 'MEDIUM'}">
            <c:set var="gridSize" value="8"/>
        </c:when>
        <c:otherwise>
            <c:set var="gridSize" value="12"/>
        </c:otherwise>
    </c:choose>

    <!-- Задаём лимит времени (в секундах) в зависимости от сложности -->
    <c:choose>
        <c:when test="${gameSession.puzzle.difficulty == 'EASY'}">
            <c:set var="timeLimit" value="480"/>
        </c:when>
        <c:when test="${gameSession.puzzle.difficulty == 'MEDIUM'}">
            <c:set var="timeLimit" value="900"/>
        </c:when>
        <c:otherwise>
            <c:set var="timeLimit" value="1200"/>
        </c:otherwise>
    </c:choose>

    <!-- Подсчёт правильно размещённых фрагментов -->
    <c:set var="placedCount" value="0"/>
    <c:forEach var="p" items="${gameSession.pieces}">
        <c:if test="${p.placedCorrectly}">
            <c:set var="placedCount" value="${placedCount + 1}"/>
        </c:if>
    </c:forEach>

    <style>
        body {
            font-family: Arial, sans-serif;
            margin: 20px;
        }
        h1 {
            text-align: center;
        }
        .container {
            display: flex;
            gap: 40px;
            justify-content: center;
        }
        .board {
            width: 500px;
            height: 500px;
            border: 2px solid #666;
            display: grid;
            grid-template-columns: repeat(${gridSize}, 1fr);
            grid-template-rows: repeat(${gridSize}, 1fr);
        }
        #targetBoard {
            border-color: #ccc;
        }
        #sourceBoard {
            border-color: #666;
        }
        .cell {
            border: 1px dashed #aaa;
            position: relative;
            overflow: hidden;
        }
        .piece {
            width: 100%;
            height: 100%;
            object-fit: cover;
            cursor: grab;
        }
    </style>
</head>
<body>

<h1>Собери пазл: ${gameSession.puzzle.name}</h1>
<p style="text-align: center;">Сложность: ${gameSession.puzzle.difficulty}</p>
<!-- Добавляем отображение таймера -->
<p id="timer" style="text-align: center;">Оставшееся время: <span id="timeDisplay"></span></p>

<div class="container">
    <div id="targetBoard" class="board">
        <c:forEach var="x" begin="0" end="${gridSize - 1}">
            <c:forEach var="y" begin="0" end="${gridSize - 1}">
                <div class="cell" id="target-${y}-${x}">
                    <c:forEach var="p" items="${gameSession.pieces}">
                        <c:if test="${p.placedCorrectly and p.correctPosition.x == x and p.correctPosition.y == y}">
                            <img id="piece-${p.id}" src="${pageContext.request.contextPath}/images/piece/${p.id}" class="piece" draggable="false"/>
                        </c:if>
                    </c:forEach>
                </div>
            </c:forEach>
        </c:forEach>
    </div>

    <div id="sourceBoard" class="board">
        <c:forEach var="x" begin="0" end="${gridSize - 1}">
            <c:forEach var="y" begin="0" end="${gridSize - 1}">
                <div class="cell" id="source-${x}-${y}">
                    <c:forEach var="p" items="${gameSession.pieces}">
                        <c:if test="${!p.placedCorrectly and p.currentPosition.x == x and p.currentPosition.y == y}">
                            <img id="piece-${p.id}" src="${pageContext.request.contextPath}/images/piece/${p.id}" class="piece" draggable="true"/>
                        </c:if>
                    </c:forEach>
                </div>
            </c:forEach>
        </c:forEach>
    </div>
</div>

<script>
    document.addEventListener('DOMContentLoaded', () => {
        const ctx = '${pageContext.request.contextPath}';
        const gridSize = Number('${gridSize}');
        const totalPieces = gridSize * gridSize;
        let placedCount = Number('${placedCount}');
        let draggedId = null;

        // Таймер: получаем лимит времени из JSP (в секундах)
        let timeRemaining = Number('${timeLimit}');
        const timerDisplay = document.getElementById('timeDisplay');

        // Функция обновления таймера
        function updateTimer() {
            const minutes = Math.floor(timeRemaining / 60);
            const seconds = timeRemaining % 60;
            timerDisplay.textContent = minutes.toString().padStart(2, '0') + ':' + seconds.toString().padStart(2, '0');
            if (timeRemaining <= 0) {
                clearInterval(timerInterval);
                alert('Время вышло');
                redirectHome('timeout');
            }
            timeRemaining--;
        }
        // Первоначальное обновление и запуск отсчёта каждую секунду
        updateTimer();
        const timerInterval = setInterval(updateTimer, 1000);

        // Обработка перетаскивания фрагментов пазла
        document.querySelectorAll('.piece').forEach(img => {
            img.addEventListener('dragstart', ev => {
                draggedId = ev.target.id.replace('piece-', '');
                ev.dataTransfer.setData('text/plain', draggedId);
            });
        });

        document.querySelectorAll('#targetBoard .cell').forEach(cell => {
            cell.addEventListener('dragover', e => e.preventDefault());
            cell.addEventListener('drop', e => {
                e.preventDefault();
                if (!draggedId) return;
                const [ , x, y ] = cell.id.split('-');
                updatePosition(draggedId, x, y)
                    .then(ok => {
                        if (ok) {
                            const img = document.getElementById('piece-' + draggedId);
                            img.draggable = false;
                            cell.appendChild(img);
                            placedCount++;
                            if (placedCount === totalPieces) {
                                redirectHome('success');
                            }
                        } else {
                            alert('Неверная ячейка. Попробуйте снова.');
                        }
                    })
                    .catch(err => {
                        console.error(err);
                        alert('Ошибка сервера. Попробуйте ещё раз.');
                    });
            });
        });

        // Функция обновления позиции фрагмента через GraphQL
        function updatePosition(id, x, y) {
            const query = `
        mutation UpdatePos($id: ID!, $x: Int!, $y: Int!){
          updatePiecePosition(pieceId:$id, newPosition:{x:$x, y:$y}){
            isPlacedCorrectly
          }
        }`;
            const variables = {
                id: parseInt(id, 10),
                x: parseInt(x, 10),
                y: parseInt(y, 10)
            };
            return fetch(ctx + '/graphql', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ query, variables })
            })
                .then(r => r.json())
                .then(res => {
                    if (res.errors) { throw res.errors[0]; }
                    return res.data.updatePiecePosition.isPlacedCorrectly;
                });
        }

        // Функция редиректа на страницу home с сообщением
        function redirectHome(msg) {
            window.location.href = ctx + '/home?msg=' + msg;
        }
    });
</script>
</body>
</html>

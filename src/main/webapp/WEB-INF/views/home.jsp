<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<html>
<head>
    <meta charset="UTF-8">
    <title>Галерея пазлов</title>
    <style>
        /* Базовая стилизация галереи */
        .gallery {
            display: flex;
            flex-wrap: wrap;
            gap: 20px;
        }
        .card {
            border: 1px solid #ddd;
            border-radius: 5px;
            padding: 10px;
            width: 200px;
            box-shadow: 2px 2px 6px rgba(0, 0, 0, 0.1);
            transition: transform 0.2s;
            text-align: center;
        }
        .card:hover {
            transform: scale(1.03);
        }
        .card img {
            width: 100%;
            height: auto;
            border-bottom: 1px solid #ddd;
            margin-bottom: 10px;
        }
        .card .card-title {
            font-weight: bold;
            margin: 5px 0;
        }
        .card .card-difficulty {
            color: #888;
            margin: 5px 0;
        }
    </style>
</head>
<body>
<h1>Галерея пазлов</h1>
<c:choose>
    <c:when test="${param.msg == 'success'}">
        <div style="padding:10px;border:1px solid #4caf50;background:#e8f5e9;color:#2e7d32;margin-bottom:15px;">
            ✅ Поздравляем! Пазл полностью собран.
        </div>
    </c:when>
    <c:when test="${param.msg == 'timeout'}">
        <div style="padding:10px;border:1px solid #ff9800;background:#fff3e0;color:#e65100;margin-bottom:15px;">
            ⏰ Время вышло. Попробуйте снова!
        </div>
    </c:when>
</c:choose>
<!-- Контейнер для карточек пазлов -->
<div class="gallery">
    <!-- Перебираем коллекцию пазлов, переданную в модель под именем puzzles -->
    <c:forEach var="puzzle" items="${puzzles}">
        <div class="card">
            <!-- Картинка пазла получаем через REST API: /images/full/{puzzle.id} -->
            <a href="${pageContext.request.contextPath}/puzzleDetail?pid=${puzzle.id}">
                <img src="${pageContext.request.contextPath}/images/full/${puzzle.id}" alt="${puzzle.name}">
            </a>
            <!-- Название пазла -->
            <div class="card-title">${puzzle.name}</div>
            <!-- Уровень сложности -->
            <div class="card-difficulty">Сложность: ${puzzle.difficulty}</div>
        </div>
    </c:forEach>
</div>

<!-- Ссылка для выхода (или навигация) -->
<p><a href="${pageContext.request.contextPath}/logout">Выйти</a></p>
</body>
</html>

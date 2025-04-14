<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<html>
<head>
    <meta charset="UTF-8">
    <title>Главная страница</title>
    <script>
        // Проверяем наличие идентификатора пользователя в sessionStorage
        document.addEventListener("DOMContentLoaded", function() {
            var playerId = sessionStorage.getItem("playerId");
            if (!playerId) {
                // Если идентификатор отсутствует, перенаправляем на страницу логина
                window.location.href = "${pageContext.request.contextPath}/login";
            } else {
                // Выводим идентификатор на страницу
                document.getElementById("playerIdDisplay").innerText = playerId;
            }
        });

        // Функция выхода: очищает sessionStorage и перенаправляет на страницу логина
        function logout() {
            sessionStorage.removeItem("playerId");
            window.location.href = "${pageContext.request.contextPath}/login";
        }
    </script>
</head>
<body>
<h2>Добро пожаловать на главную страницу!</h2>
<p>Ваш ID пользователя: <span id="playerIdDisplay"></span></p>
<p>Здесь будет основное содержимое приложения.</p>
<p><a href="#" onclick="logout()">Выйти</a></p>
</body>
</html>

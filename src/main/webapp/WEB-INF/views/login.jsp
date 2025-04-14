<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<html>
<head>
    <meta charset="UTF-8">
    <title>Логин</title>
    <script>
        document.addEventListener("DOMContentLoaded", function () {
            document.getElementById("loginForm").addEventListener("submit", function (e) {
                e.preventDefault();
                var username = document.getElementById("username").value;
                var password = document.getElementById("password").value;
                // Формируем GraphQL запрос для аутентификации пользователя
                var query = `
            query {
              authenticatePlayer(player: {username: "${username}", password: "${password}"})
            }
          `;
                fetch("${pageContext.request.contextPath}/graphql", {
                    method: "POST",
                    headers: {"Content-Type": "application/json"},
                    body: JSON.stringify({query: query})
                })
                    .then(function (response) {
                        return response.json();
                    })
                    .then(function (data) {
                        console.log("Результат аутентификации", data);
                        if (data.data && data.data.authenticatePlayer) {
                            // Сохраняем идентификатор пользователя в sessionStorage
                            var playerId = data.data.authenticatePlayer;
                            sessionStorage.setItem("playerId", playerId);
                            // Перенаправляем на главную страницу
                            window.location.href = "${pageContext.request.contextPath}/home";
                        } else {
                            alert("Неверный email или пароль.");
                        }
                    })
                    .catch(function (error) {
                        console.error("Ошибка аутентификации", error);
                        alert("Ошибка аутентификации. Повторите попытку.");
                    });
            });
        });
    </script>
</head>
<body>
<h2>Логин</h2>
<form id="loginForm">
    <label for="username">Email:</label>
    <input type="email" id="username" name="username" required/><br/><br/>

    <label for="password">Пароль:</label>
    <input type="password" id="password" name="password" required/><br/><br/>

    <input type="submit" value="Войти"/>
</form>
<br/>
<p>Еще не зарегистрированы? <a href="${pageContext.request.contextPath}/register">Регистрация</a></p>
</body>
</html>

<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<html>
<head>
    <meta charset="UTF-8">
    <title>Регистрация</title>
    <script>
        document.addEventListener("DOMContentLoaded", function() {
            document.getElementById("regForm").addEventListener("submit", function(e) {
                e.preventDefault();
                var username = document.getElementById("username").value;
                var password = document.getElementById("password").value;
                // Формируем GraphQL мутацию для создания нового пользователя
                var query = `
            mutation {
              newPlayer(player: {username: "${username}", password: "${password}"}) {
                id
                username
              }
            }
          `;
                fetch("${pageContext.request.contextPath}/graphql", {
                    method: "POST",
                    headers: { "Content-Type": "application/json" },
                    body: JSON.stringify({ query: query })
                })
                    .then(function(response) { return response.json(); })
                    .then(function(data) {
                        console.log("Регистрация успешно выполнена", data);
                        // Перенаправляем на страницу логина после регистрации
                        window.location.href = "${pageContext.request.contextPath}/login";
                    })
                    .catch(function(error) {
                        console.error("Ошибка регистрации", error);
                        alert("Ошибка регистрации. Повторите попытку.");
                    });
            });
        });
    </script>
</head>
<body>
<h2>Регистрация нового пользователя</h2>
<form id="regForm">
    <label for="username">Email:</label>
    <input type="email" id="username" name="username" required/><br/><br/>

    <label for="password">Пароль:</label>
    <input type="password" id="password" name="password" required/><br/><br/>

    <input type="submit" value="Зарегистрироваться"/>
</form>
<br/>
<p>Уже зарегистрированы? <a href="${pageContext.request.contextPath}/login">Войти</a></p>
</body>
</html>

package com.ssau.mozaikagame.filter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

@Component
@Slf4j
public class SessionFilter implements Filter {

  @Override
  public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse,
                       FilterChain filterChain) throws IOException, ServletException {
    HttpServletRequest request = (HttpServletRequest) servletRequest;
    HttpServletResponse response = (HttpServletResponse) servletResponse;

    String uri = request.getRequestURI();
    // Определяем эндпоинты, которые не требуют проверки сессии
    if (uri.endsWith("/login") || uri.endsWith("/register")
            || uri.contains("/graphql") || uri.contains("/graphiql") || uri.contains("/favicon.ico") && isAuthenticationQuery(request)) {
      filterChain.doFilter(servletRequest, servletResponse);
      return;
    }

    // Если атрибут playerId отсутствует, перенаправляем на страницу логина
    if (request.getSession().getAttribute("playerId") == null) {
      log.info("Пользователь не авторизован при доступе к: {}", uri);
      response.sendRedirect(request.getContextPath() + "/login");
      return;
    }
    filterChain.doFilter(servletRequest, servletResponse);
  }

  /**
   * Этот метод можно доработать для более точного определения GraphQL-запросов,
   * требующих авторизации (например, если в теле запроса содержится операция,
   * отличная от аутентификации).
   * В упрощённом варианте здесь можно вернуть true, чтобы не блокировать запросы по /graphql.
   */
  private boolean isAuthenticationQuery(HttpServletRequest request) {
    // Для упрощения на данный момент пропускаем все запросы к /graphql
    return true;
  }
}

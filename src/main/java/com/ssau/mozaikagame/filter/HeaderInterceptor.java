package com.ssau.mozaikagame.filter;

import lombok.RequiredArgsConstructor;
import org.springframework.graphql.server.WebGraphQlInterceptor;
import org.springframework.graphql.server.WebGraphQlRequest;
import org.springframework.graphql.server.WebGraphQlResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public class HeaderInterceptor implements WebGraphQlInterceptor {

  @Override
  public Mono<WebGraphQlResponse> intercept(WebGraphQlRequest request, Chain chain) {
    // Извлекаем HttpHeaders из запроса
    HttpHeaders headers = request.getHeaders();
    // Добавляем HttpHeaders в GraphQL контекст под ключом "httpHeaders"
    request.configureExecutionInput((executionInput, builder) ->
            builder.graphQLContext(ctx -> ctx.put("httpHeaders", headers)).build()
    );
    return chain.next(request);
  }
}

package com.travel.airport.filters;

import java.nio.charset.StandardCharsets;

import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Component
public class SessionValidateFilter extends AbstractGatewayFilterFactory<SessionValidateFilter.Config> {

  public SessionValidateFilter() {
    super(SessionValidateFilter.Config.class);
  }

  public static class Config {
  }

  @Override
  public GatewayFilter apply(Config config) {
    return (exchange, chain) -> {
      return exchange.getSession().flatMap(session -> {
        Object uuid = session.getAttributes().get("uuid");
        if (uuid != null) {
          return chain.filter(exchange.mutate().request(builder -> builder.header("uuid", uuid.toString())).build());
        } else {
          return unauthorized(exchange);
        }
      });
    };
  }

  private Mono<Void> unauthorized(ServerWebExchange exchange) {
    exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
    exchange.getResponse().getHeaders().add("Content-Type", "application/json");

    byte[] bytes = "{\"message\":\"Unauthorized\"}".getBytes(StandardCharsets.UTF_8);
    DataBuffer buffer = exchange.getResponse().bufferFactory().wrap(bytes);

    return exchange.getResponse().writeWith(Flux.just(buffer));
  }
}

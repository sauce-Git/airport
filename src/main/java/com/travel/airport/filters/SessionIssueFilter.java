package com.travel.airport.filters;

import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;

import reactor.core.publisher.Mono;

@Component
public class SessionIssueFilter extends AbstractGatewayFilterFactory<SessionIssueFilter.Config> {

  public SessionIssueFilter() {
    super(SessionIssueFilter.Config.class);
  }

  public static class Config {
  }

  /**
   * GatewayFilter
   * Save the uuid from the response header to the session.
   */
  @Override
  public GatewayFilter apply(Config config) {
    return (exchange, chain) -> {
      return exchange.getSession().flatMap(session -> {
        return chain.filter(exchange).then(Mono.fromRunnable(() -> {
          ServerHttpResponse response = exchange.getResponse();
          HttpStatusCode statusCode = response.getStatusCode();
          String uuid = response.getHeaders().getFirst("uuid");
          if (uuid != null && (statusCode.equals(HttpStatus.OK) || statusCode.equals(HttpStatus.CREATED))) {
            session.getAttributes().put("uuid", uuid);
            response.getHeaders().remove("uuid");
          } else if (uuid == null && (statusCode.equals(HttpStatus.OK) || statusCode.equals(HttpStatus.CREATED))) {
            session.getAttributes().remove("uuid");
            response.setStatusCode(HttpStatus.UNAUTHORIZED);
          }
        }));
      });
    };
  }
}

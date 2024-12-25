package com.travel.airport.filters;

import java.util.List;
import lombok.Getter;
import lombok.Setter;
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

  @Override
  public List<String> shortcutFieldOrder() {
    return List.of("sessionAttributes");
  }

  @Getter
  @Setter
  public static class Config {
    private List<String> sessionAttribute = List.of("uuid");
    private Boolean isRequired = true;
  }

  /**
   * GatewayFilter Save the uuid from the response header to the session.
   */
  @Override
  public GatewayFilter apply(Config config) {
    return (exchange, chain) -> exchange.getSession().flatMap(session -> {
      return chain.filter(exchange).then(Mono.fromRunnable(() -> {
        ServerHttpResponse response = exchange.getResponse();
        HttpStatusCode statusCode = response.getStatusCode();
        config.getSessionAttribute().forEach(attribute -> {
          String value = response.getHeaders().getFirst(attribute);
          if (value != null) {
            session.getAttributes().put(attribute, value);
            response.getHeaders().remove(attribute);
          }
        });

        if (config.getIsRequired()
            && (statusCode == HttpStatus.OK || statusCode == HttpStatus.CREATED)
            && config.getSessionAttribute().stream()
                .anyMatch(attribute -> session.getAttributes().get(attribute) == null)) {
          config.getSessionAttribute()
              .forEach(attribute -> session.getAttributes().remove(attribute));
          response.setStatusCode(HttpStatus.UNAUTHORIZED);
        }
      }));
    });
  }
}

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

  @Getter
  @Setter
  public static class Config {
    private List<String> attributes = List.of();
    private List<String> requiredAttributes = List.of();
  }

  @Override
  public List<String> shortcutFieldOrder() {
    return List.of("attributes", "requiredAttributes");
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

        // Required attributes must be present in the response
        config.getRequiredAttributes().forEach(attribute -> {
          String value = response.getHeaders().getFirst(attribute);
          if (value == null) {
            // If attribute is required and the response is successful, return Unauthorized
            if (statusCode == HttpStatus.OK || statusCode == HttpStatus.CREATED) {
              response.setStatusCode(HttpStatus.UNAUTHORIZED);
            }
          } else {
            // Save the attribute to the session
            session.getAttributes().put(attribute, value);
            response.getHeaders().remove(attribute);
          }
        });

        // Save the attributes to the session
        config.getAttributes().forEach(attribute -> {
          String value = response.getHeaders().getFirst(attribute);
          if (value == null) {
            session.getAttributes().remove(attribute);
          } else {
            // Save the attribute to the session
            session.getAttributes().put(attribute, value);
            response.getHeaders().remove(attribute);
          }
        });
      }));
    });
  }
}

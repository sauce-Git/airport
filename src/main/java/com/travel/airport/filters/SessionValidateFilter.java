package com.travel.airport.filters;

import java.util.List;

import lombok.Getter;
import lombok.Setter;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.stereotype.Component;
import com.travel.airport.error.ErrorHandler;

@Component
public class SessionValidateFilter extends
    AbstractGatewayFilterFactory<SessionValidateFilter.Config> {

  public SessionValidateFilter() {
    super(Config.class);
  }

  @Setter
  @Getter
  public static class Config {
    private List<String> attributes = List.of();
    private List<String> requiredAttributes = List.of();
  }

  @Override
  public List<String> shortcutFieldOrder() {
    return List.of("attributes", "requiredAttributes");
  }

  /**
   * GatewayFilter Validate the session. If the session is valid, add the uuid to
   * the request
   * header. If the session is invalid, return 401 Unauthorized.
   */
  @Override
  public GatewayFilter apply(Config config) {
    return (exchange, chain) -> exchange.getSession().flatMap(session -> {
      if (config.getRequiredAttributes().stream()
          .anyMatch(attribute -> session.getAttributes().get(attribute) == null)) {
        return ErrorHandler.unauthorized(exchange);
      }

      config.getRequiredAttributes().forEach(attribute -> {
        Object value = session.getAttributes().get(attribute);
        if (value != null) {
          exchange.getRequest().mutate().header(attribute, value.toString());
        }
      });

      config.getAttributes().forEach(attribute -> {
        Object value = session.getAttributes().get(attribute);
        if (value != null) {
          exchange.getRequest().mutate().header(attribute, value.toString());
        }
      });

      return chain.filter(exchange);
    });
  }

}

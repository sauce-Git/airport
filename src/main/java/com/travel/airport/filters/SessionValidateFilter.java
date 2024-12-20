package com.travel.airport.filters;

import java.util.List;

import lombok.Getter;
import lombok.Setter;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.stereotype.Component;
import com.travel.airport.error.ErrorHandler;

@Component
public class SessionValidateFilter extends AbstractGatewayFilterFactory<SessionValidateFilter.Config> {

  public SessionValidateFilter() {
    super(Config.class);
  }

  @Override
  public List<String> shortcutFieldOrder() {
    return List.of("sessionAttributes");
  }

  /**
   * GatewayFilter
   * Validate the session.
   * If the session is valid, add the uuid to the request header.
   * If the session is invalid, return 401 Unauthorized.
   */
  @Override
  public GatewayFilter apply(Config config) {
    return (exchange, chain) -> {
      return exchange.getSession().flatMap(session -> {
        System.out.println("config.getSessionAttributes() = " + config.getSessionAttributes());
        Object uuid = session.getAttributes().get("uuid");
        if (uuid != null) {
          return chain.filter(exchange.mutate().request(builder -> builder.header("uuid", uuid.toString())).build());
        } else {
          return ErrorHandler.unauthorized(exchange);
        }
      });
    };
  }

  @Setter
  @Getter
  public static class Config {
    private List<String> sessionAttributes;
  }

}

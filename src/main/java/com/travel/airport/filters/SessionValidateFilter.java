package com.travel.airport.filters;

import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.stereotype.Component;
import com.travel.airport.error.ErrorHandler;

@Component
public class SessionValidateFilter extends AbstractGatewayFilterFactory<SessionValidateFilter.Config> {

  public SessionValidateFilter() {
    super(SessionValidateFilter.Config.class);
  }

  public static class Config {
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
        Object uuid = session.getAttributes().get("uuid");
        if (uuid != null) {
          return chain.filter(exchange.mutate().request(builder -> builder.header("uuid", uuid.toString())).build());
        } else {
          return ErrorHandler.unauthorized(exchange);
        }
      });
    };
  }
}

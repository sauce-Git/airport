package com.travel.airport.filters;

import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.stereotype.Component;

import reactor.core.publisher.Mono;

@Component
public class SessionIssueFilter extends AbstractGatewayFilterFactory<SessionIssueFilter.Config> {

  public SessionIssueFilter() {
    super(SessionIssueFilter.Config.class);
  }

  public static class Config {
  }

  @Override
  public GatewayFilter apply(Config config) {
    return (exchange, chain) -> {
      return exchange.getSession().flatMap(session -> {
        return chain.filter(exchange).then(Mono.fromRunnable(() -> {
          String uuid = exchange.getResponse().getHeaders().getFirst("uuid");
          if (uuid != null) {
            session.getAttributes().put("uuid", uuid);
            exchange.getResponse().getHeaders().remove("uuid");
          }
        }));
      });
    };
  }
}
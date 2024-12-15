package com.travel.airport.routes;

import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.travel.airport.config.RoutesConfig;
import com.travel.airport.filters.SessionIssueFilter;
import com.travel.airport.filters.SessionValidateFilter;

import lombok.AllArgsConstructor;

@Configuration
@AllArgsConstructor
public class ByuljogakRoutes {

  private final RoutesConfig routesConfig;
  private final SessionIssueFilter sessionIssueFilter;
  private final SessionValidateFilter sessionValidateFilter;

  @Bean
  RouteLocator authRouteLocator(RouteLocatorBuilder builder) {
    return builder.routes()
        .route("auth", r -> r.path("/auth/**")
            .filters(f -> f
                .rewritePath("/auth/(?<segment>.*)", "/${segment}")
                .filter(sessionIssueFilter.apply(sessionIssueFilter.newConfig())))
            .uri(routesConfig.getAuthUri()))
        .route("tarot", r -> r.path("/tarot/**")
            .filters(f -> f
                .rewritePath("/tarot/(?<segment>.*)", "/${segment}")
                .filter(sessionValidateFilter.apply(sessionValidateFilter.newConfig())))
            .uri(routesConfig.getAuthUri()))
        .build();
  }
}

package com.travel.airport.error;

import java.nio.charset.StandardCharsets;

import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ServerWebExchange;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public class ErrorHandler {

  /**
   * Error response. Return the status and message.
   *
   * @param exchange
   * @param status
   * @param message
   * @return Mono<Void>
   */
  public static Mono<Void> error(ServerWebExchange exchange, HttpStatus status, String message) {
    exchange.getResponse().setStatusCode(status);
    exchange.getResponse().getHeaders().add("Content-Type", "application/json");

    byte[] bytes = ("{\"message\":\"" + message + "\"}").getBytes(StandardCharsets.UTF_8);
    DataBuffer buffer = exchange.getResponse().bufferFactory().wrap(bytes);

    return exchange.getResponse().writeWith(Flux.just(buffer));
  }

  /**
   * Unauthorized response. Return 401 Unauthorized with a message.
   * 
   * @param exchange
   * @return Mono<Void>
   */
  public static Mono<Void> unauthorized(ServerWebExchange exchange) {
    return error(exchange, HttpStatus.UNAUTHORIZED, "Unauthorized");
  }

  /**
   * Unauthorized response. Return 401 Unauthorized with a message.
   * 
   * @param exchange
   * @param message
   * @return Mono<Void>
   */
  public static Mono<Void> unauthorized(ServerWebExchange exchange, String message) {
    return error(exchange, HttpStatus.UNAUTHORIZED, message);
  }

  /**
   * Forbidden response. Return 403 Forbidden with a message.
   * 
   * @param exchange
   * @return Mono<Void>
   */
  public static Mono<Void> forbidden(ServerWebExchange exchange) {
    return error(exchange, HttpStatus.FORBIDDEN, "Forbidden");
  }

  /**
   * Forbidden response. Return 403 Forbidden with a message.
   * 
   * @param exchange
   * @param message
   * @return Mono<Void>
   */
  public static Mono<Void> forbidden(ServerWebExchange exchange, String message) {
    return error(exchange, HttpStatus.FORBIDDEN, message);
  }
}

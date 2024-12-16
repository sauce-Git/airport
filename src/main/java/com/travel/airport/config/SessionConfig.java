package com.travel.airport.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.ReactiveRedisConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.session.data.redis.config.annotation.web.server.EnableRedisIndexedWebSession;
import org.springframework.web.server.session.CookieWebSessionIdResolver;
import org.springframework.web.server.session.WebSessionIdResolver;

@Configuration
@EnableRedisIndexedWebSession
public class SessionConfig {

  @Value("${spring.data.redis.host}")
  private String redisHost;

  @Value("${spring.data.redis.port}")
  private int redisPort;

  @Bean
  LettuceConnectionFactory connectionFactory() {
    return new LettuceConnectionFactory(redisHost, redisPort);
  }

  @Bean
  WebSessionIdResolver webSessionIdResolver() {
    CookieWebSessionIdResolver resolver = new CookieWebSessionIdResolver();
    resolver.setCookieName("SESSION");
    resolver.addCookieInitializer((builder) -> builder.path("/").httpOnly(true));
    resolver.addCookieInitializer((builder) -> builder.sameSite("Strict"));
    resolver.addCookieInitializer((builder) -> builder.secure(true));
    resolver.addCookieInitializer((builder) -> builder.maxAge(60 * 60 * 3));
    return resolver;
  }

  @Bean
  ReactiveRedisTemplate<String, Object> reactiveRedisTemplate(ReactiveRedisConnectionFactory factory) {
    RedisSerializationContext<String, Object> serializationContext = RedisSerializationContext
        .<String, Object>newSerializationContext(new StringRedisSerializer())
        .hashKey(new StringRedisSerializer())
        .hashValue(new GenericJackson2JsonRedisSerializer())
        .build();

    return new ReactiveRedisTemplate<>(factory, serializationContext);
  }
}

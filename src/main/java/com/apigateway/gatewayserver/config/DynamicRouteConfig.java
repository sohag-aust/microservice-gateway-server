package com.apigateway.gatewayserver.config;

import com.apigateway.gatewayserver.utils.Const;
import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.cloud.gateway.filter.ratelimit.RedisRateLimiter;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.time.LocalDateTime;

@Configuration
public class DynamicRouteConfig {

    @Bean
    public RouteLocator customRouteLocator(RouteLocatorBuilder routeLocatorBuilder) {
        return routeLocatorBuilder.routes()
                .route(p -> p
                        .path(Const.DynamicPath.ACCOUNTS)
                        .filters( f -> f.rewritePath(Const.RewritePath.ACCOUNTS_REWRITE_PATH,"/${segment}")
                                .addResponseHeader("X-Response-Time", LocalDateTime.now().toString())
                                .circuitBreaker(
                                        config -> config
                                                .setName(Const.CircuitBreakerNames.ACCOUNTS_CIRCUIT_BREAKER)
//                                                .setFallbackUri("forward:/contactSupport")
                                )
                        )
                        .uri(Const.LoadBalancedPredicates.ACCOUNTS_LB))
                .route(p -> p
                        .path(Const.DynamicPath.LOANS)
                        .filters( f -> f.rewritePath(Const.RewritePath.LOANS_REWRITE_PATH,"/${segment}")
                                .addResponseHeader("X-Response-Time", LocalDateTime.now().toString())
                                .retry(retryConfig -> retryConfig.setRetries(3)
                                        .setMethods(HttpMethod.GET)
                                        .setBackoff(Duration.ofMillis(100),Duration.ofMillis(1000),2,true))
                        )
                        .uri(Const.LoadBalancedPredicates.LOANS_LB))
                .route(p -> p
                        .path(Const.DynamicPath.CARDS)
                        .filters( f -> f.rewritePath(Const.RewritePath.CARDS_REWRITE_PATH,"/${segment}")
                                .addResponseHeader("X-Response-Time", LocalDateTime.now().toString())
                                .requestRateLimiter(
                                        config ->
                                                config
                                                        .setRateLimiter(redisRateLimiter())
                                                        .setKeyResolver(userKeyResolver())
                                )
                        )
                        .uri(Const.LoadBalancedPredicates.CARDS_LB))
                .build();
    }

    // it is using Token-Bucket Algorithm
    @Bean
    public RedisRateLimiter redisRateLimiter() {
        return new RedisRateLimiter(2, 4, 1);
    }

    @Bean
    public KeyResolver userKeyResolver() {
        return exchange -> Mono.justOrEmpty(exchange.getRequest().getHeaders().getFirst("user"))
                .defaultIfEmpty("anonymous");
    }
}

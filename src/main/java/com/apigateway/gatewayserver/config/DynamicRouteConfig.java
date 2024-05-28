package com.apigateway.gatewayserver.config;

import com.apigateway.gatewayserver.utils.Const;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.LocalDateTime;

@Configuration
public class DynamicRouteConfig {

    @Bean
    public RouteLocator customRouteLocator(RouteLocatorBuilder routeLocatorBuilder) {
        return routeLocatorBuilder.routes()
                .route(p -> p
                        .path(Const.DynamicPath.ACCOUNTS)
                        .filters( f -> f.rewritePath(Const.RewritePath.ACCOUNTS_REWRITE_PATH,"/${segment}")
                                .addResponseHeader("X-Response-Time", LocalDateTime.now().toString()))
                        .uri(Const.LoadBalanced.ACCOUNTS_LB))
                .route(p -> p
                        .path(Const.DynamicPath.LOANS)
                        .filters( f -> f.rewritePath(Const.RewritePath.LOANS_REWRITE_PATH,"/${segment}")
                                .addResponseHeader("X-Response-Time", LocalDateTime.now().toString()))
                        .uri(Const.LoadBalanced.LOANS_LB))
                .route(p -> p
                        .path(Const.DynamicPath.CARDS)
                        .filters( f -> f.rewritePath(Const.RewritePath.CARDS_REWRITE_PATH,"/${segment}")
                                .addResponseHeader("X-Response-Time", LocalDateTime.now().toString()))
                        .uri(Const.LoadBalanced.CARDS_LB))
                .build();
    }
}

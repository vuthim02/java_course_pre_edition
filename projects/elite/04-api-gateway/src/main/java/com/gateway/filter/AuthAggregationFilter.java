package com.gateway.filter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.core.Ordered;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

public class AuthAggregationFilter implements GatewayFilter, Ordered {
    private static final Logger log = LoggerFactory.getLogger(AuthAggregationFilter.class);

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        return ReactiveSecurityContextHolder.getContext()
            .flatMap(ctx -> {
                Authentication auth = ctx.getAuthentication();
                if (auth instanceof JwtAuthenticationToken jwtAuth) {
                    Jwt jwt = jwtAuth.getToken();
                    ServerWebExchange mutated = exchange.mutate()
                        .request(r -> r
                            .header("X-User-Id", jwt.getSubject())
                            .header("X-User-Email", jwt.getClaimAsString("email"))
                            .header("X-User-Roles", String.join(",", jwtAuth.getAuthorities().stream()
                                .map(a -> a.getAuthority()).toList()))
                            .header("X-Auth-Source", "jwt"))
                        .build();
                    return chain.filter(mutated);
                }
                return chain.filter(exchange);
            })
            .switchIfEmpty(chain.filter(exchange));
    }

    @Override
    public int getOrder() {
        return -2;
    }
}

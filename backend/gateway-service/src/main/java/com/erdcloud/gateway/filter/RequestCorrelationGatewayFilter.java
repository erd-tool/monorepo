package com.erdcloud.gateway.filter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
public class RequestCorrelationGatewayFilter implements GlobalFilter, Ordered {

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String requestId = GatewayRequestCorrelationMdc.ensureRequestId(
            exchange.getRequest().getHeaders().getFirst(GatewayHeaderConstants.REQUEST_ID)
        );

        ServerHttpRequest mutatedRequest = exchange.getRequest().mutate()
            .headers(headers -> headers.set(GatewayHeaderConstants.REQUEST_ID, requestId))
            .build();

        exchange.getResponse().getHeaders().set(GatewayHeaderConstants.REQUEST_ID, requestId);

        GatewayRequestCorrelationMdc.Scope scope = GatewayRequestCorrelationMdc.open(requestId, null, null);
        return chain.filter(exchange.mutate().request(mutatedRequest).build())
            .doFinally(signalType -> scope.close());
    }

    @Override
    public int getOrder() {
        return -200;
    }
}

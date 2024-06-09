package com.apigateway.gatewayserver.utils;

public class Const {

    public static class DynamicPath {
        public static final String ACCOUNTS = "/eazybank/accounts/**";
        public static final String LOANS = "/eazybank/loans/**";
        public static final String CARDS = "/eazybank/cards/**";
    }

    public static class RewritePath {
        public static final String ACCOUNTS_REWRITE_PATH = "/eazybank/accounts/(?<segment>.*)";
        public static final String LOANS_REWRITE_PATH = "/eazybank/loans/(?<segment>.*)";
        public static final String CARDS_REWRITE_PATH = "/eazybank/cards/(?<segment>.*)";
    }

    public static class LoadBalancedPredicates {
        public static final String ACCOUNTS_LB = "lb://ACCOUNTS";
        public static final String LOANS_LB = "lb://LOANS";
        public static final String CARDS_LB = "lb://CARDS";
    }

    public static class CircuitBreakerNames {
        public static final String ACCOUNTS_CIRCUIT_BREAKER = "accountsCircuitBreaker";
        public static final String LOANS_CIRCUIT_BREAKER = "loansCircuitBreaker";
        public static final String CARDS_CIRCUIT_BREAKER = "cardsCircuitBreaker";
    }
}

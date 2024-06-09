### Spring Cloud Gateway | API Gateway | Edge Server

### When to run this service
    Running order : configserver -> eurekaserver -> accountsservice -> cardsservice -> loanservice -> gatewayservice

### To find routes for all the microservices from gateway service
    http://localhost:8072/actuator/gateway/routes

### Test gateway service
    
    1. goto postman gatewayserver section
    2. hit : http://localhost:8072/ACCOUNTS/api/create       this api to call accountservice and create account via gateway service
    3. here in the api ACCOUNTS means the service name in the eureka server, all the service name in the eureka server are in capital letters
    4. to avoid capital letters in the api we should need to add a config in application.yml
        lower-case-service-id: true
        in the cloud->gateway->discovery section
    5. re run gateway server and hit : http://localhost:8072/accounts/api/create  , for create account with small letters account service name

### So how gateway server route request based on microservices name to api ?

    http://localhost:8072/actuator/gateway/routes
    after hitting this api we will see there is a section like
    
    {
    "predicate": "Paths: [/ACCOUNTS/**], match trailing slash: true",
    "metadata": {
    "management.port": "8080"
    },
    "route_id": "ReactiveCompositeDiscoveryClient_ACCOUNTS",
    "filters": [
    "[[RewritePath /ACCOUNTS/?(?<remaining>.*) = '/${remaining}'], order = 1]"
    ],
    "uri": "lb://ACCOUNTS",
    "order": 0
    },

    so here, in filters section, we can see based on service name it routes the extra api path to that service api


### Implementing Custom-Routing-using-Spring-Cloud-Gateway

    suppose instead of the path : http://localhost:8072/accounts/api/create,
    we need a dynamic path : http://localhost:8072/mycompany/accounts/api/create , which is not exposed by gateway server automatically
    so, we need to implement custom logic for it

### Check all routes from gateway server

    http://localhost:8072/actuator/gateway/routes

    hit the API, to check all routes of services, so there we will see our custom route also with default route, like for account microservices
    there will be 2 routes,
    1. {
        "predicate": "Paths: [/eazybank/accounts/**], match trailing slash: true",
        "route_id": "4a187043-07b6-478f-a09e-2339e44540c7",
        "filters": [
        "[[RewritePath /eazybank/accounts/(?<segment>.*) = '/${segment}'], order = 0]",
        "[[AddResponseHeader X-Response-Time = '2024-05-28T11:59:27.047915283'], order = 0]"
        ],
        "uri": "lb://ACCOUNTS",
        "order": 0
       }
        
        this one is our cutom route
        and

    2. {
        "predicate": "Paths: [/ACCOUNTS/**], match trailing slash: true",
        "metadata": {
        "management.port": "8080"
        },
        "route_id": "ReactiveCompositeDiscoveryClient_ACCOUNTS",
        "filters": [
        "[[RewritePath /ACCOUNTS/?(?<remaining>.*) = '/${remaining}'], order = 1]"
        ],
        "uri": "lb://ACCOUNTS",
        "order": 0
       },

        this one is default one

    so, if we don't want to see the default one, as it will not work, because we have already enabled the custom route,
    so, we need to remove the default route from the gateway route path
    so, setting enabled: false in the application.yml will work

      cloud:
        gateway:
          discovery:
            locator:
              enabled: false

### AddResponseHeader in the filter
    to track the response time of gateway server from microservices, we have added a addResponseHeader to track time.
    we can check it in the API response headers section.

    like the addResponseHeader, we can add many numbers of filter in the filter section of customRoute bean


### Section :: 9.3 ::  Implementing Cross cutting concerns for tracing and logging using Gateway Server

    Business need or use case:

        Like a request comes into gateway server, and gateway server to account microservices, and account microservices
        to cards and loans microservices. and response back in the reverse order

        So, we will set a co-relation id from gateway server and pass it to microservices, and from returning back the response,
        the same co-relation id will be back in the response header.
        
        So, if there any issue or problem, we can track with the co-relation id from which microservices it is not returning
        and creating issues.

        we will also implement some logging concept here.

    We should change in the microservices like: accounts, cards microservices too, to accept the request header from gatewayservice


### Section - 10 : Circuit Breaker Pattern | Retry Pattern | Rate Limiter Pattern

    *** Circuit breaker pattern using resiliency 4J ***

    1. slidingWindowSize: This parameter defines the size of the sliding window used by the circuit breaker to monitor 
    the success or failure of calls. In this case, it's set to 10, meaning the circuit breaker will consider 
    the last 10 calls when evaluating whether to trip open from closed.

    2. permittedNumberOfCallsInHalfOpenState: This parameter sets the maximum number of permitted calls when the circuit breaker 
    is in the half-open state. The half-open state is entered after the circuit breaker has been in the open state for a certain duration. 
    In this case, it's set to 2, meaning that only 2 calls will be allowed through the circuit breaker when it's in the half-open state.

    3. failureRateThreshold: This parameter determines the threshold for the failure rate at which the circuit breaker will trip open. 
    It's set to 50, meaning if the failure rate (percentage of failed calls) exceeds 50% within the sliding window, the circuit breaker will open.

    4. waitDurationInOpenState: This parameter specifies the duration for which the circuit breaker will remain in the open state 
    before transitioning to the half-open state and allowing calls to pass through again. It's set to 10000 milliseconds (10 seconds), 
    meaning the circuit breaker will stay open for 10 seconds before attempting to half-open.


    # Check cirtcuitBreaker in actuator endpoint : http://localhost:8072/actuator/circuitbreakers
    # Hit accounts api for more information of cirtcuitBreaker : http://localhost:8072/eazybank/accounts/api/contact-info
    # Check circuitBreaker event of accountsCircuitBreaker : http://localhost:8072/actuator/circuitbreakerevents?name=accountsCircuitBreaker
    
    
    *** Check cirtcuitBreaker is working fine for account microservices ***
    1. As we are looking for /api/contact-info api in account microservice. so putting a debugger on that api and don't release the debugger 
    from response on that api. and run the account microservices in debug mode

    2. the hit the api : http://localhost:8072/eazybank/accounts/api/contact-info

    3. Looking into this api : http://localhost:8072/actuator/circuitbreakerevents?name=accountsCircuitBreaker
    for checking the events

    4. Looking into this api : http://localhost:8072/actuator/circuitbreakers
    for checking the states

    5. The postman response will be 504 , which is gateway timeout, as firstly the circuit breaker is closed
    6. after that the postman api response will be 503, which is account microservice is unavailable as the circuit breaker
    is opened

    7. wait 10 seconds , then hit the api : http://localhost:8072/eazybank/accounts/api/contact-info
    again, then we can see postman response is 504 , which is gateway timeout again, because the circuit breaker will be in
    half-open state as per config, where we mention 10sec in open state : waitDurationInOpenState: 10000

    8. If we want to see the response from account microservice, we just need to release the break point

    *** Adding the FallBack mechanism to send a custom error message ***
    1. without the fallback functionality, we are getting some 504 / 503 error codes, and long error messages,
    which is not good

    2. So we are implementing the fallback mechanism to send a custom error message


    *** Setting http connection timeout and response timeout ***
    1. Inside the application.yml file, setting http connection timeout and response timeout configuration
    httpclient:
        connect-timeout: 1000
        response-timeout: 2s

    this timeout will be applicable for loans and cards microservices. it won't be applicable for the account microservices
    because, the account microservices has attached the circuit breaker logic, which has default http timeout configurations
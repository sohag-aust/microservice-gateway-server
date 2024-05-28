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

    and also setting , lower-case-service-id: false  because we want microservices name in capital letters

### AddResponseHeader in the filter
    to track the response time of gateway server from microservices, we have added a addResponseHeader to track time.
    we can check it in the API response headers section.

    like the addResponseHeader, we can add many numbers of filter in the filter section of customRoute bean
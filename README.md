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
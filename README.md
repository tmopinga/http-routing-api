# Routing API Exercise

This API receives HTTP requests and send them to an instance of application API. The API will receive the response from the application API and send it back to the client.

### Getting Started
#### Prerequisites
 - Java 11
 - mvn

#### Installation

1. Clone the repository
  ```
  git clone https://github.com/tmopinga/http-routing-api.git
  ```
2. Install dependencies

```mvn clean install```

3. Update application.properties if necessary

4. Run

```mvn clean spring-boot:run```


### How to configure the list of Application APIs

Update *serverList* in application.properties.
Value format should be a comma-separated host:port

Example
```
serverList=localhost:4000,localhost:4001,localhost:4002
```

### How does the API choose which Application API to send request to?

The API receives a request and chooses which application API instance to send the request to on a **‘round robin’** basis. Therefore, if you have 3 instances of the application api then the first request goes to instance 1, the second to instance 2, the third to instance 3 etc.

### How would the API handle if one of the Application APIs goes down or goes slowly?

##### Scheduled Health Check

A function to check the health of each server in the _serverList_ is scheduled to run every set interval after start up.


If the Application API is unreachable or returns a non 200 status response, it will be removed from the list of active servers. If in the next run it was found to be healthy again, it will be added back to the end of the list.

**Configuration**

```
healthCheck.fixedDelayInMs=15000
healthCheck.connectionTimeoutInMs=15000
healthCheck.readTimeoutInMs=15000
```
**fixedDelayInMs**
Fixed duration between the end of the last health check execution, and the start of the next execution. Fixed delay is used since the previous execution has to be completed before running again. 


**connectionTimeoutInMs**
Time for Routing API to establish connection to Application API instance

**readTimeoutInMs**
Time for Application API to return the response to Routing API

### For Improvements
- Use of LoadBalancer interface to easily add other implementations other than Round Robin
- Configurable health check run and path
- Different class for health check

#### For considerations/Edge cases
- Unhealthy application instance will be removed from the list and will lose its original position
- Health check is still running and server returned by RoundRobinBalancer is unreachable
- Waiting time for each health check to run if there are too many servers in the list

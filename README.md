# Coding challenge
**Carpark Ubi**

## Domain vocabulary:
EV - electric vehicle.<br/>
CP - charging point, an element in an infrastructure that supplies electric energy for the recharging of electric
vehicles.

## Problem details:
The task is to implement a simple application to manage the charging points installed at Carpark Ubi. Carpark Ubi has 10
charging points installed. When a car is connected it consumes either 20 Amperes (fast charging) or 10 Amperes (slow
charging). Carpark Ubi installation has an overall current input of 100 Amperes so it can support fast charging for a
maximum of 5 cars or slow charging for a maximum of 10 cars at one time. A charge point notifies the application when a
car is plugged or unplugged. The application must distribute the available current of 100 Amperes among the charging
points so that when possible all cars use fast charging and when the current is not sufficient some cars are switched to
slow charging. Cars which were connected earlier have lower priority than those which were connected later. The
application must also provide a report with a current state of each charging point returning a list of charging point,
status (free or occupied) and - if occupied – the consumed current.

## Requirements:
1. The solution must be implemented as a Spring Boot application with Java.
2. We need to be able to start it and run tests.
3. BIZ logic needs to be implemented correctly.
4. Interaction with the APP needs to happen through well-defined REST APIs.
4. Include at least one unit test and one integration test.
3. Solution needs to be thread safe.

## Examples:

```
CP1 sends a notification that a car is plugged
Report: 
CP1 OCCUPIED 20A
CP2 AVAILABLE
...
CP10 AVAILABLE
```

```
CP1, CP2, CP3, CP4, CP5 and CP6 send notification that a car is plugged
Report:
CP1 OCCUPIED 10A
CP2 OCCUPIED 10A
CP3 OCCUPIED 20A
CP4 OCCUPIED 20A
CP5 OCCUPIED 20A
CP6 OCCUPIED 20A
CP7 AVAILABLE
...
CP10 AVAILABLE
```

## Deliverables:
Link to the git repository with the implementation and the documentation on how to call the API (Swagger/Postman
collection/text description).
Please add any details about your ideas and considerations to this README and add it to the repository.


## Considerations:
- The implemented solution uses the `PUT` HTTP operation as the trigger for all changes in the connection operations. This
  could be something changed depending on the approach taken by the team as consensus for the API. Instead, what is 
  known as a [controller resource](http://uniknow.github.io/AgileDev/site/0.1.9-SNAPSHOT/parent/rest/resource-archetypes.html#:~:text=controller%20resource)
  could be used to address the connect/disconnect operations. As a personal preference, I try to solve as many problems
  as possible using the standard meaning for HTTP methods (I feel this represents states better than having actions in
  endpoints as suggested by the controller approach, this may be considered a typical RESTful philosophical discussion).
  In any case it's important to recognise the power of different approaches when the advantages are significant in real
  life scenarios.
- Implementing the operation as a `PUT` request means that the request should be Idempotent. This was also taken care of
  in the implementation. (Initially I forgot about it, but this was successfully patched in the end).
- With the current implementation when connecting an already connected device the connection order is not altered. If 
  this is desired, a disconnection operation should be performed in between the two connections.
- The name/identifier of the charge points is assigned automatically for pragmatical purposes. In real life these ids
  could be set to more meaningful values or could be customised from the UI with operations to manipulate the properties
  of the park-car.
- The logic of the application is kept in the singleton service class **CarparkUbi**, this class could persist its
  members to be able to handle failures in a real life scenario. In that case the connect/disconnect action logs could
  be persisted allowing to re-build the state of the Carpark-Ubi charging site.
- An alternative option to have a persistent state for the carpark would be to persist the *queue* information for the
  `chargingQueue` object after each modification.
- Concurrency is handled by making the `connect` and `disconnect` methods synchronised in the class `CarparkUbi`.
- The tests implemented for the controller could be considered by some as integration tests. To me those are just 
  *fancy* unit tests that check some spring context components like the exception handling. The project in reality 
  doesn't require integration tests, hence no *real* integration tests were implemented.
- The rest endpoint was implemented thinking of the possibility to extend the functionality to more carparks beside the
  ubi carpark. In that case the `carparks` endpoint would be used with a different identifier. For now the only
  identifier working for a carpark is **ubi**. The handling for this specific carpark was hard-coded in the application.
- Exception handling in Rest endpoints is a topic that deserves discussion and careful design. In this case the
  implementation is throwing directly the Spring `ResponseStatusException`. In case of having the application ready for
  production it would be good to have consistent bodies with detailed information about the errors. This would need
  further work using a `ControllerAdvice` approach.
- The `ChargingPointNotFoundException` was created within the scope of a prototyping application. In a final production 
  version the exception handling should be thought more carefully having better descriptions and better integration with
  the controllers. One could think that all the exception handling could be done using only the Spring class 
  `ResponseStatusException`. Nonetheless, this would not be a good approach as it would mix the logic belonging to the 
  REST controller into the Business classes.
- The creation of the `ChargingPointDTO` class may seem useless in this context as the solution could be implemented
  using only the class `ChargingPoint` in the `services` package. Nevertheless, this separation would be useful in a
  more complete application to add validations to the object used in the controller and simply have a better separation
  of concerns in the application.
- The scope of the task was very small, hence adding logging was skipped. Some `debug` logging could be useful in the
  business classes. Nevertheless, as the endpoints are expected to be called often, having `info` logs in a task like
  this one would overflow the logs with not so useful information. This task doesn't require access logs just to see 
  how the endpoint is called or how the connection queue behaves.
- The chosen documentation method was Spring RestDocs. I like this method because real objects are generated for 
  both requests and responses. This method doesn't present the interactive approach that Swagger allows, but having a
  generated `curl` command available is enough to compensate this missing feature.

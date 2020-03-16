# Food Ordering Demo - Scala - Lagom
Based on the demo application delivered by Axon in [Java](https://github.com/AxonIQ/food-ordering-demo)

## Running the application
Lagom delivers modules for Cassandra and Kafka to spin up with the dev application.
This means that the full stack of needed components can be started with a single command.

Starting the application using sbt:
```
> sbt runAll
```

## Using the application
Request all food carts:
```
> curl -H "Content-Type: application/json" -X GET http://localhost:9000/foodCarts
```

Create a new cart:
```
> curl -H "Content-Type: application/json" -X GET http://localhost:9000/foodCart/create
```

Add an item to the cart:
```
> curl -H "Content-Type: application/json" -X GET http://localhost:9000/foodCart/{generatedCartId}/select/{someProductUUID}/quantity/10
```

Overview of the cart:
```
> curl -H "Content-Type: application/json" -X GET http://localhost:9000/foodCart/{generatedCartId}
```
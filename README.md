Run with `gradle bootRun` and navigate to [GraphiQL locally](http://localhost:8080/graphiql)
and execute
```graphql
subscription {
  getFooSubscriptionReactive {
    x
  }
}
```
then cancel the subscription and observe the ReactorRejectedExecutionException: Scheduler unavailable

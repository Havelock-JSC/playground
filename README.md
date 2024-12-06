Run with `gradle bootRun` and navigate to [GraphiQL locally](http://localhost:8080/graphiql)
and execute
```graphql
subscription s {
  getFooSubscription {
    x
  }
}
```

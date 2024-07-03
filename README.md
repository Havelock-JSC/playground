To reproduce the problem run with `gradle bootRun` and navigate to [GraphiQL locally](http://localhost:8080/graphiql)
and execute
```graphql
subscription s {
  getFooSubscription {
    x
    y
  }
}
```

This works when `@SchemaMapping` is used instead or when the Spring Boot version is reverted to `3.2.7`

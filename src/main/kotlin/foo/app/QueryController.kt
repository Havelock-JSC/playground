package foo.app

import graphql.model.Foo
import org.springframework.graphql.data.method.annotation.BatchMapping
import org.springframework.graphql.data.method.annotation.QueryMapping
import org.springframework.stereotype.Controller

@Controller
class QueryController {
    @QueryMapping
    fun getFoo() = Foo(5)

    // This works
//    @SchemaMapping(typeName = "Foo", field = "y")
//    fun fooY(foo: Foo) = 7

    // This hangs (doesn't return anything)
    @BatchMapping(typeName = "Foo", field = "y")
    fun fooY(foos: List<Foo>) = foos.associateWith { 7 }
}

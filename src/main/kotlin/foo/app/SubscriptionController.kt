package foo.app

import graphql.model.Foo
import org.springframework.graphql.data.method.annotation.SubscriptionMapping
import org.springframework.stereotype.Controller
import reactor.core.publisher.Flux

@Controller
class SubscriptionController {
    @SubscriptionMapping
    fun getFooSubscription() = Flux.just(Foo(3))
}

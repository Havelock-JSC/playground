package foo.app

import graphql.model.Foo
import kotlin.concurrent.thread
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.springframework.graphql.data.method.annotation.SchemaMapping
import org.springframework.graphql.data.method.annotation.SubscriptionMapping
import org.springframework.stereotype.Controller
import reactor.core.publisher.Flux
import java.time.Duration
import java.util.concurrent.Executors
import java.util.concurrent.Semaphore
import java.util.concurrent.TimeUnit

@Controller
class SubscriptionController {
    @SubscriptionMapping
    fun getFooSubscriptionReactive(): Flux<Foo> {
        var shouldSend = true
        return Flux.create(
            { sink ->
                val emissions = Semaphore(Int.MAX_VALUE)
                emissions.acquire(Int.MAX_VALUE)
                sink.onRequest {
                    emissions.release(it.toInt())
                }
                // In another thread, in our case in a thread processing messages coming from Google PubSub
                thread {
                    while (shouldSend) {
                        Thread.sleep(Duration.ofMillis(20))
                        if (emissions.tryAcquire(3, TimeUnit.SECONDS)) {
                            sink.next(Foo(""))
                        }
                    }
                }
                sink.onCancel {
                    shouldSend = false
                }
            }
        )
    }

    @SchemaMapping(typeName = "Foo", field = "x")
    fun fooX(foo: Foo): String {
        Thread.sleep(1000)
        return "x"
    }

    companion object {
        val coroutineContext = Executors.newVirtualThreadPerTaskExecutor().asCoroutineDispatcher()
    }
}

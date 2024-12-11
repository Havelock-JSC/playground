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
import org.springframework.graphql.data.method.annotation.SubscriptionMapping
import org.springframework.stereotype.Controller
import reactor.core.publisher.Flux
import reactor.core.publisher.FluxSink
import java.time.Duration
import java.util.concurrent.Executors
import java.util.concurrent.Semaphore
import java.util.concurrent.TimeUnit

@Controller
class SubscriptionController {
    @SubscriptionMapping
    fun getFooSubscription(): Flow<Foo> {
        var callback: (suspend () -> Unit)? = null
        var i = 0
        val flow = callbackFlow {
            callback = {
                try {
                    withTimeout(3.seconds) {
                        send(Foo(++i))
                    }
                    println("successfully sent")
                } catch (exception: CancellationException) {
                    // The client disconnected, nothing we can do
                    println("unsubscribed" + exception.message)
                } catch (exception: Throwable) {
                    println("unexpected error " + exception.message)
                }
            }
            awaitClose { callback = null }
        }

        // In another thread, in our case in a thread processing messages coming from Google PubSub
        thread {
            while (true) {
                Thread.sleep(Duration.ofMillis(20))
                callback?.let {
                    println("sending")
                    runBlocking(coroutineContext) {
                        it.invoke()
                    }
                }
            }
        }

        return flow
    }

    @SubscriptionMapping
    fun getFooSubscriptionReactive(): Flux<Foo> {
        var shouldSend = true
        return Flux.create(
            { sink ->
                val emissions = Semaphore(Int.MAX_VALUE)
                emissions.acquire(Int.MAX_VALUE)
                sink.onRequest {
                    println("requested $it")
                    emissions.release(it.toInt())
                }
                // In another thread, in our case in a thread processing messages coming from Google PubSub
                thread {
                    var i = 0
                    while (shouldSend) {
                        Thread.sleep(Duration.ofMillis(20))
                        if (emissions.tryAcquire(3, TimeUnit.SECONDS)) {
                            println("sending")
                            sink.next(Foo(++i))
                            println("successfully sent")
                        }
                    }
                }
                sink.onCancel {
                    shouldSend = false
                    println("subscription canceled")
                }
            },
            FluxSink.OverflowStrategy.BUFFER
        )
    }

    companion object {
        val coroutineContext = Executors.newVirtualThreadPerTaskExecutor().asCoroutineDispatcher()
    }
}

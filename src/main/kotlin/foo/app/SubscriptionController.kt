package foo.app

import graphql.model.Foo
import kotlin.concurrent.thread
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ClosedSendChannelException
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.runBlocking
import org.springframework.graphql.data.method.annotation.SubscriptionMapping
import org.springframework.stereotype.Controller
import java.time.Duration
import java.util.concurrent.Executors

@Controller
class SubscriptionController {
    @SubscriptionMapping
    fun getFooSubscription(): Flow<Foo> {
        val channel = Channel<Foo>()
        // In another thread, in our case in a thread processing messages coming from Google PubSub
        thread {
            emit(channel)
        }
        return channel.consumeAsFlow()
    }

    private fun emit(channel: SendChannel<Foo>) {
        var i = 0
        while (true) {
            Thread.sleep(Duration.ofSeconds(1))
            runBlocking(coroutineContext) {
                try {
                    channel.send(Foo(++i))
                } catch (exception: ClosedSendChannelException) {
                    // The client disconnected, nothing we can do
                } catch (exception: CancellationException) {
                    // The client disconnected, nothing we can do
                }
            }
        }
    }

    companion object {
        val coroutineContext = Executors.newVirtualThreadPerTaskExecutor().asCoroutineDispatcher()
    }
}

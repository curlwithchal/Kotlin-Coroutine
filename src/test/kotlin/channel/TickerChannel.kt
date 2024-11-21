package channel

import kotlinx.coroutines.ObsoleteCoroutinesApi
import kotlinx.coroutines.channels.ticker
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeoutOrNull
import kotlin.test.Test

class TickerChannel {

    @OptIn(ObsoleteCoroutinesApi::class)
    @Test
    fun testTickerChannel() = runBlocking<Unit> {
        val tickerChannel = ticker(delayMillis = 200L, initialDelayMillis = 0L)

        var nextElement = withTimeoutOrNull(1) { tickerChannel.receive() }
        println("Initial Element is available immediately: $nextElement") // not initial delay

        nextElement = withTimeoutOrNull(100L) { tickerChannel.receive() } // all subsequent element have 200ms delay
        println("Next Element is not ready in 100 ms: $nextElement")

        nextElement = withTimeoutOrNull(110) { tickerChannel.receive() }
        println("Next Element is ready in 200 ms: $nextElement")

        //Emulate large consumption delays
        println("consumer pauses for 300ms")
        delay(300L)
        //Next Element is available immediately
        nextElement = withTimeoutOrNull(1) { tickerChannel.receive() }
        println("Next Element is available immediately after large consumer delay : $nextElement")

        // Note that the pause between `receive` calls is taken into account and next element arrives faster
        nextElement = withTimeoutOrNull(120) { tickerChannel.receive() }
        println("Next element is ready in 100ms after consumer pause in 300ms: $nextElement")

        tickerChannel.cancel() // indicate that no more elements are needed
    }

}
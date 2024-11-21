package channel

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.channels.produce
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test

class Channels {

    @Test
    fun testChannel() = runBlocking<Unit> {
        val channel = Channel<Int>()
        println("Start Task")
        launch {
            for (i in 1..5) {
                println("Send: $i")
                channel.send(i * i)
            }
        }
        repeat(5) {
            println(channel.receive())
        }
        println("Done Task")
    }

    @Test
    fun testClosing() = runBlocking<Unit> {
        val channel = Channel<Int>()
        println("Start Task")
        delay(1000L)
        launch {
            for (i in 1..5) channel.send(i * i)
            channel.close()
        }

        for (x in channel) println("Retrieve $x")
        println("Done")
    }

    // build coroutineScope producers replace example above with ext func
    @OptIn(ExperimentalCoroutinesApi::class)
    fun CoroutineScope.produceSquared(): ReceiveChannel<Int> = produce {
        for (x in 1..5) {
            println("Sent: $x")
            send(x * x)
        }
    }

    @Test
    fun buildChannelProducer() = runBlocking<Unit> {
        val produce = produceSquared()
        produce.consumeEach { println("Receive Result: $it") }
        println("Done")
    }

    //Pipelines some processing and some result
    //note: Pipelines possible infinity stream values

    // Produces
    @OptIn(ExperimentalCoroutinesApi::class)
    fun CoroutineScope.produceNumbers(): ReceiveChannel<Int> = produce {
        var x = 1
        while (true) send(x++) // infinity stream loop starting
    }

    // Consumes
    // doing some processing and other result
    @OptIn(ExperimentalCoroutinesApi::class)
    fun CoroutineScope.squared(number: ReceiveChannel<Int>): ReceiveChannel<Int> = produce {
        for (i in number) send(i * i)
    }

    @Test
    fun testPipelines() = runBlocking<Unit> {
        val numbers = produceNumbers()
        val squares = squared(numbers)

        repeat(5) {
            println(squares.receive())
        }
        println("Done Pipeline")
        coroutineContext.cancelChildren() // cancel all children coroutines
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    fun CoroutineScope.numbersFrom(start: Int) = produce<Int> {
        var x = start
        while (true) {
            send(x++)
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    fun CoroutineScope.filter(numbers: ReceiveChannel<Int>, prime: Int) = produce<Int> {
        for (i in numbers) if (i % prime != 0) send(i)
    }

    @Test
    fun testPrimeNumberPipelines() = runBlocking<Unit> {
        var cur = numbersFrom(2)
        repeat(10) {
            val prime = cur.receive()
            println(prime)
            cur = filter(cur, prime)
        }
        coroutineContext.cancelChildren()
    }

    //Fan-out
    //Note multiple coroutines may receive from the same channel
    @OptIn(ExperimentalCoroutinesApi::class)
    fun CoroutineScope.produceNumbersTwo(): ReceiveChannel<Int> = produce {
        var x = 1
        while (true) {
            send(x++)
            delay(100) // await 0.1s
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    fun CoroutineScope.launchProcessor(id: Int, channel: ReceiveChannel<Int>) = launch {
        for (message in channel) println("Processor #$id: Received $message")
    }

    @Test
    fun testFanOut() = runBlocking<Unit> {
        val producer = produceNumbersTwo()
        repeat(5) {
            launchProcessor(it, producer)
        }
        delay(950)
        producer.cancel() // terminating coroutine producer and thus kill them all
    }


    //Fan-in
    //Note multiple coroutines may send to the same channel

    suspend fun sendString(chanel: SendChannel<String>, send: String, time: Long) {
        while (true) {
            delay(time)
            chanel.send(send)
        }
    }

    @Test
    fun testFanIn() = runBlocking<Unit> {
        val channel = Channel<String>()
        launch {
            sendString(channel, "FOO", 300L)
            Dispatchers.Unconfined
        }
        launch {
            sendString(channel, "BAR", 600L)
        }
        repeat(10) {
            println(channel.receive())
        }
        coroutineContext.cancelChildren() // cancel all children Job to let main finish
    }

}
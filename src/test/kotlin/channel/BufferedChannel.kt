package channel

import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlin.system.measureTimeMillis
import kotlin.test.Test

class BufferedChannel {

    @Test
    fun testBuffer() = runBlocking<Unit> {
        val channel = Channel<Int>(5) // Create buffered channel

        val sender = launch {
            repeat(10) {
                println("Sending $it") // print before sending each element
                channel.send(it) // will suspend when buffer is full
            }
        }
        // don't receive anything... just wait...
        delay(1000L)
        sender.cancel() // cancel sender
    }

    suspend fun player(name: String, table: Channel<Ball>){
        for (ball in table){ // receive the ball in a loop
            ball.hits++
            println("$name $ball")
            delay(300L)
            table.send(ball) // send the ball back
        }
    }

    // Channel are fair
    @Test
    fun testChannelBufferedFifo() = runBlocking<Unit> {
        val measure = measureTimeMillis {
            val channel = Channel<Ball>() // shared table
            launch{player("ping", channel)}
            launch{player("pong", channel)}
            channel.send(Ball(0)) // serve the ball
            delay(1000L)
        }
        coroutineContext.cancelChildren()
        println("Done task ${measure}.ms")
    }
}

data class Ball(var hits: Int)
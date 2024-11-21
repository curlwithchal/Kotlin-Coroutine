package cancellation

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import kotlinx.coroutines.withTimeoutOrNull
import kotlin.test.Test

class CancellationAndTimeOut {

    fun cancelTimeout() = runTest {
        val job = launch {
            repeat(1000) { i ->
                println("I'm sleeping $i ...")
                delay(500L)
            }
        }
        delay(1500L)
        println("Waiting for job to finish")
        job.cancel()
        job.join()
        println("Done Task")
    }


    fun cancelAndJoin() = runBlocking {
        val startTime = System.currentTimeMillis()

        // Dispatcher Parallelism -> Default or IO
        val job = launch(Dispatchers.Default) {
            var nextPrintTime = startTime
            var i = 0
            while (isActive) { // computation loop cancellable -> CoroutineScope
                // print message twice a second
                if (System.currentTimeMillis() >= nextPrintTime) {
                    println("job: I'm sleeping ${i++} ...")
                    nextPrintTime += 500L
                }
            }
        }

        delay(10000L)
        println("Im Tired of waiting!")
        job.cancelAndJoin()
        println("Done Task")
    }

    @Test
    fun testCancelTimeout() {
        cancelTimeout()
    }

    @Test
    fun testCancelJoin() {
        cancelAndJoin()
    }

    fun observeCancelAndJoin() = runBlocking {
        val job = launch {
            repeat(5) { i ->
                try {
                    println("I'm sleeping $i ...")
                    delay(500L)
                } catch (e: Exception) {
                    e.message
                }
            }
        }
        delay(1300L)
        println("I'm Sad for waiting...")
        job.cancelAndJoin()
        println("Done Task")
    }

    @Test
    fun testObserveCancelAndJoin() {
        observeCancelAndJoin()
    }

    fun nonCancellable() = runBlocking {
        val job = launch {
            try {
                repeat(1000) { i ->
                    println("Job: I'm sleeping $i ...")
                    delay(500L)
                }
            } finally {
                withContext(NonCancellable) {
                    println("Job: I'm running finally")
                    delay(1000L)
                    println("Job: And I've just delayed for 1 sec because i'm non-cancellable")
                }
            }
        }

        delay(1300L)
        println("main: I'm tired of waiting!")
        job.cancelAndJoin()
        println("Done Task")
    }

    @Test
    fun testNonCancellable() {
        nonCancellable()
    }

    // return thrown Exception Error
    fun cancelTimeOut() = runBlocking {
        withTimeout(1300L) {
            try {
                repeat(1000) { i ->
                    println("I'm sleeping $i ...")
                    delay(500L)
                }
            } catch (e: TimeoutCancellationException) {
                println(e.message)
            }
        }
    }

    // return thrown null Exception immediately
    fun cancelWithTimeOutOrNull() = runBlocking {
        val result = withTimeoutOrNull(1300L) {
            repeat(1000) { i ->
                println("I'm sleeping $i ...")
                delay(500L)
            }
            "DONE"
        }
        println("result: $result")
    }


    @Test
    fun testCancelTimeOut() {
        cancelTimeOut()
    }

    @Test
    fun testCancelWithTimeOutOrNull() {
        cancelWithTimeOutOrNull()
    }

}

package asyncFlow

import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.cancellable
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.reduce
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.transform
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeoutOrNull
import kotlin.test.Test

class AsyncFLow {

    fun simpleFLow(): Flow<Int> = flow {
        println("Flow Started")
        for (i in 1..5) {
            delay(100L)
            emit(i)
        }
    }


    @Test
    fun testSimpleFlow() = runBlocking {
        println("Calling simpleFlow")
        val flow = simpleFLow()
        println("Calling collect")
        flow.collect { println(it) }
        println("Calling collect again")
        flow.collect { println(it) }
    }

    @Test
    fun testCancellationFlow() = runBlocking {
        // Cancellation occurs
        withTimeoutOrNull(300L) {
            simpleFLow().collect { println(it) }
        }
        println("DONE..")
    }

    fun cancellationCheckFlow(): Flow<Int> = flow {
        for (i in 1..5) {
            println("Emitting $i")
            emit(i)
        }
    }

    @Test
    fun testCancellationCheckFlow() = runBlocking {
        cancellationCheckFlow().collect { i ->
            when (i) {
                3 -> {
                    cancel()
                    println(i)
                }
            }

        }
    }

    @Test
    fun testNoCheckForCancellationOperator() = runBlocking {
        // note: all print the loop and cancellation from return runBlocking
        // cancellation not check for value 3 example code above because, this is flow operators ().asFlow do not cancellable
        (1..5).asFlow().collect { value ->
            if (value == 3) cancel()
            println(value)
        }
    }

    @Test
    fun testCheckForCancellationOperator() = runBlocking {
        // note -> cancellation occurs equal value 3
        (1..5).asFlow().cancellable().collect { value ->
            if (value == 3) cancel()
            println(value)
        }
    }

    // Flow Builders Example
    @Test
    fun testExtRandomFLow() = runBlocking {
        flowOf(1, 2, 3).collect { println(it) }
        println("-------------")
        // convert flow
        (1..10).asFlow().collect { value -> println(value) }
    }

    suspend fun performRequest(request: Int): String {
        delay(1000L) // imitate log-running async work
        return "response $request"
    }

    @Test
    fun testPerformRequest() = runBlocking {
        println("Start Request")
        (1..5).asFlow().map { request -> performRequest(request) }
            .collect { response -> println(response) }
        println("Done Request")
    }

    @Test
    fun testPerformRequestReplaceTransformCollector() = runBlocking {
        println("Start Request")
        (1..5).asFlow().transform {
            emit("Making request $it")
            emit(performRequest(it))
        }.collect { println(it) }
        println("Done Request")
    }

    fun numbers(): Flow<Int> = flow {
        try {
            emit(1)
            emit(2)
            println("Not Execute this line")
            emit(3)
        } finally {
            println("Finally in numbers")
        }
    }

    @Test
    fun testSizeLimiting() = runBlocking {
        numbers().take(2).collect { println(it) }

    }

    // note: flow collection is sequential default
    @Test
    fun flowNotSequential() = runBlocking {
        (1..5).asFlow()
            .filter { i ->
                println("filter $i")
                i % 2 == 0
            }
            .map {
                println("Map $it")
                "string $it"
            }
            .collect { println("collect $it") }
    }


}
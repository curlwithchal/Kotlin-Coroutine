package composingSuspendingFunc

import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlin.system.measureTimeMillis


suspend fun foo(): Int {
    println("foo invoked")
    delay(1000L)
    return 10
}

suspend fun bar(): Int {
    println("bar invoked")
    delay(1000L)
    return 20
}

fun computeResult() = runBlocking {
    val time = measureTimeMillis {
        val one = foo()
        val two = bar()
        println("The answer is ${one + two}")
    }
    println("Completed in $time ms")
}

fun computeResultAsync() = runBlocking {
    val time = measureTimeMillis {
        val one = async { foo() }
        val two = async { bar() }
        println("The answer is ${one.await() + two.await()}")
    }
    println("Completed in $time ms")
}

fun computeResultAsyncLazy() = runBlocking {
    val time = measureTimeMillis {
        val one = async(start = CoroutineStart.LAZY) { foo() }
        val two = async(start = CoroutineStart.LAZY) { bar() }
        one.start() // -> not Invoke run sequential Default
        two.start() // -> not Invoke run sequential Default
        println("The answer is ${one.await() + two.await()}")
    }
    println("Completed in $time ms")

}

// Global Scope -> additional @OptIn because not memory leak And Resource Leak cause
@OptIn(DelicateCoroutinesApi::class)
fun somethingFooAsync() = GlobalScope.async {
    foo()
}

@OptIn(DelicateCoroutinesApi::class)
fun somethingBarAsync() = GlobalScope.async {
    bar()
}

fun runGlobalScopeAsync() {
    // note -> considered do async
    // still running operation Thread Background
    val time = measureTimeMillis {
        // we can initiate async actions outside of a coroutine
        val one = somethingFooAsync()
        val two = somethingBarAsync()

        // but waiting for a result must involve either suspending or blocking.
        // here we use `runBlocking { ... }` to block the main thread while waiting for the result
        runBlocking {
            println("The answer is ${one.await() + two.await()}")
        }
    }
    print("Complete the time in $time ms")
}

// Replacement Considered Global Scope Async Concurrent
suspend fun somethingFooAsyncReplacement(): Int {
    println("foo invoked")
    delay(1000L)
    return 10
}

suspend fun somethingBarAsyncReplacement(): Int {
    println("bar invoked")
    delay(1000L)
    return 20
}

// extract async with coroutineScope
suspend fun concurrentSum(): Int = coroutineScope {
    val one = async { somethingFooAsyncReplacement() }
    val two = async { somethingBarAsyncReplacement() }
    one.await() + two.await()
}

fun runGlobalScopeAsyncReplacement() = runBlocking {
    val time = measureTimeMillis {
        println("The answer is ${concurrentSum()}")
    }
    println("Completed in $time ms")
}

//Cancellation is always propagated through coroutines hierarchy:
suspend fun failedConcurrentSum(): Int = coroutineScope {
    val one = async<Int> {
        try {
            delay(Long.MAX_VALUE)
            42
        } finally {
            println("First child was cancelled")
        }
    }

    // throw exception occurs
    val two = async<Int> {
        println("Second child throws Exception")
        throw ArithmeticException()
    }
    one.await() + two.await()
}

fun testFailedConcurrentSum() = runBlocking {
    val time = measureTimeMillis {
        try {
            failedConcurrentSum()
        } catch (e: ArithmeticException) {
            println("Computation Failed with ArithmeticException")
        }
    }
    println("Completed in $time ms")
}


fun main(args: Array<String>) {
//    computeResult() // Sequential Default
//    computeResultAsync() // faster -> because concurrent
//    computeResultAsyncLazy() // Async Lazy
//    runGlobalScopeAsync()
//    runGlobalScopeAsyncReplacement()
    testFailedConcurrentSum()
}

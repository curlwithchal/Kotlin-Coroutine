package exceptionsHandling

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import kotlinx.coroutines.yield
import java.io.IOException
import kotlin.test.Test

class ExceptionsHandling {


    @OptIn(DelicateCoroutinesApi::class)
    @Test
    fun testExceptionRootCoroutine() = runBlocking<Unit> {

        val job = GlobalScope.launch {
            println("Throwing exception from launch")
            throw IndexOutOfBoundsException()
        }

        job.join()
        println("Joined Failed Job")
        val deferred = GlobalScope.async {
            println("Throwing exception from async")
            throw ArithmeticException() // Nothing is printed, relying on user to call await
        }
        try {
            deferred.await()
            println("Unreached")
        } catch (e: ArithmeticException) {
            println("Caught ArithmeticException")
        }
    }

    @OptIn(DelicateCoroutinesApi::class)
    @Test
    fun testExceptionRootCoroutineTwo() = runBlocking<Unit> {
        val handler = CoroutineExceptionHandler { _, exception ->
            println("CoroutineExceptionHandler got $exception")
        }

        val job = GlobalScope.launch(handler) {
            throw AssertionError()
        }

        val deferred = GlobalScope.async(handler) {
            throw ArithmeticException() // not print, relying user to call await()
        }

        joinAll(job, deferred)
    }

    // note -> CoroutineExceptionHandler it the does not make sense to install runBlocking follow example above
    // CoroutineExceptionHandler created with GlobalScope root
    @Test
    fun testCancellationException() = runBlocking<Unit> {
        val job = launch {
            val child = launch {
                try {
                    delay(Long.MAX_VALUE)
                } finally {
                    println("Child is cancelled")
                }
            }
            yield()
            println("Cancelling child")
            child.cancel()
            child.join()
            yield()
            println("Parent is not cancelled")
        }
        job.join()
        println("Done Task")
    }

    @OptIn(DelicateCoroutinesApi::class)
    @Test
    fun testCancellationExceptionTwo() = runBlocking<Unit> {
        val handler = CoroutineExceptionHandler { _, exception ->
            println("CoroutineExceptionHandler got $exception")
        }

        val job = GlobalScope.launch(handler) {
            launch { // first child coroutine
                try {
                    delay(Long.MAX_VALUE)
                } finally {
                    withContext(NonCancellable) {
                        println("Children are cancelled, but exception is not handled until all children terminate")
                        delay(100)
                        println("The first child finished its non cancellable block")
                    }
                }
            }

            launch { // second child coroutine
                delay(10)
                println("Second Child Throw Exception")
                throw ArithmeticException()
            }
        }
        job.join()
        println("Done Task")
    }

    @OptIn(DelicateCoroutinesApi::class)
    @Test
    fun testExceptionAggregationMultipleChildren() = runBlocking<Unit> {
        val handler = CoroutineExceptionHandler { _, exception ->
            println("CoroutineExceptionHandler got $exception with suppressed ${exception.suppressed.contentToString()}")
        }

        val job = GlobalScope.launch(handler) {
            launch {
                try {
                    delay(Long.MAX_VALUE) // it gets cancelled when another sibling fails with IOException
                } finally {
                    throw ArithmeticException() // the second exception
                }
            }
            launch {
                delay(100)
                throw IOException() // the first exception
            }
            delay(Long.MAX_VALUE)
        }
        job.join()
    }

    @OptIn(DelicateCoroutinesApi::class)
    @Test
    fun testExceptionUnwrapped() = runBlocking {
        val handler = CoroutineExceptionHandler { _, exception ->
            println("CoroutineExceptionHandler got $exception")
        }

        val job = GlobalScope.launch(handler) {
            val innerJob = launch { // all the stack coroutines of coroutines will get cancelled
                launch {
                    throw IOException() // the original exception
                }
            }
            try {
                innerJob.join()
            } catch (e: CancellationException) {
                println("Rethrowing CancellationException with Original cause")
                throw e // cancellation exception is rethrown, yet the original IOException gets to the handler
            }
        }
        job.join()
    }
}
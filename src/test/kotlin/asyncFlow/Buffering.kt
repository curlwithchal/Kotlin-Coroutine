package asyncFlow

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.buffer
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.conflate
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.onEmpty
import kotlinx.coroutines.runBlocking
import kotlin.system.measureTimeMillis
import kotlin.test.Test

class Buffering {


    fun simpleBuffering(): Flow<Int> = flow {
        for (i in 1..3) {
            delay(100L) // pretend async waiting 100 ms
            emit(i)
        }
    }

    //  take around 1000 or more  ms
    @Test
    fun testSimpleBuffering() = runBlocking<Unit> {
        val measureTimeMillis = measureTimeMillis {
            simpleBuffering().collect {
                delay(300L)
                println(it)
            }
        }
        println("Done Task ${measureTimeMillis}ms")
    }

    // take around 1000 ms
    @Test
    fun testExtFunSimpleBuffering() = runBlocking<Unit> {
        val measureTimeMillis = measureTimeMillis {
            simpleBuffering()
                .buffer() // buffer emissions don't await
                .collect {
                    delay(300L)
                    println(it)
                }
        }
        println("Done Task ${measureTimeMillis}ms")
    }

    //Conflation -> skip each value
    // status update value attempt
    // processing last value
    @Test
    fun testExtFunConflation() = runBlocking<Unit> {
        val measureTimeMillis = measureTimeMillis {
            simpleBuffering()
                .conflate()
                .collect {
                    delay(300L)
                    println(it)
                }
        }
        println("Done Task ${measureTimeMillis}ms")
    }

    // processing last value
    @Test
    fun testReplaceExtFunConflation() = runBlocking<Unit> {
        val measureTimeMillis = measureTimeMillis {
            simpleBuffering()
                .collectLatest {
                    println("Collecting $it")
                    delay(300L)
                    println("Done Value $it")
                }
        }
        println("Done Task ${measureTimeMillis}ms")
    }
}
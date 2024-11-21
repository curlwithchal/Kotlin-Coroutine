package asyncFlow

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.runBlocking
import kotlin.test.Test


class FlowExceptions {

    fun simpleFlow(): Flow<String> = flow {
        for (i in 1..3) {
            println("Emitting $i")
            emit(i)
        }
    }.map { value ->
        check(value <= 1) { "Crashed $value" }
        "string $value"
    }

    @Test
    fun testSimpleFlow() = runBlocking<Unit> {
        try {
            simpleFlow().collect { value ->
                println(value)
//                check(value <= 1) { "Collected $value" }//
            }
        } catch (e: Throwable) {
            println("Throw $e")
        }
    }

    // Not Try Catch
    @Test
    fun testFlowTransparency() = runBlocking<Unit> {
        simpleFlow()
            .catch { e -> println("Caught $e") }
            .collect { println(it) }
    }

    fun simpleTwo(): Flow<Int> = flow {
        for (i in 1..3) {
            println("Emitting $i")
            emit(i)
        }
    }

    @Test
    fun testSimpleTwo() = runBlocking<Unit> {
        simpleTwo()
            .catch { e -> println("Caught $e") }// does not catch downstream exception
            .collect { value ->
                check(value <= 1) { "Collected $value" } // interrupt
                println("value: $value")
            }
    }

    // catch declarative
    @Test
    fun testCatchingWithoutParameter() = runBlocking<Unit> {
        simpleTwo()
            .onEach { value ->
                check(value <= 1) { "Collected $value" }
                println(value)
            }
            .catch { e -> println("Caught $e") }
            .collect()
    }


}
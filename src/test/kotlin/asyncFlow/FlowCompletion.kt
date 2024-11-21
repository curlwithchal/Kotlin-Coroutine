package asyncFlow

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test

class FlowCompletion {

    fun imperativeFinally(): Flow<Int> = (1..3).asFlow()

    fun simpleFlowError(): Flow<Int> = flow {
        for (i in 1..3) {
            emit(i)
            throw RuntimeException()
        }
    }

    @Test
    fun testImperativeFinally() = runBlocking<Unit> {
        try {
            imperativeFinally().collect {
                value -> println(value)
            }
        }finally {
            println("Done")
        }
    }

    @Test
    fun testDeclarativeHandling() = runBlocking<Unit> {
        imperativeFinally()
            .onCompletion {println("Done")}
            .collect{println(it)}
    }

    @Test
    fun testDeclarativeHandlingError() = runBlocking<Unit> {
        simpleFlowError()
            .onCompletion {cause -> if(cause != null) println("Flow Completed Exception $cause")}
            .catch { cause ->  println("Caught Exception")}
            .collect{println(it)}
    }

    // Note: upstream only successfully completion without cancellation or failure
    @Test
    fun testSuccessfullyCompletion() = runBlocking<Unit> {
        imperativeFinally()
            .onCompletion { cause -> println("Flow Completed $cause") }
            .collect{value ->
                check(value <= 1) {"Collected $value"} // aborted downstream exception
                println("Value: $value")
            }
    }

}
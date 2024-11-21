package asyncFlow

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.cancellable
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlin.test.Test

class LaunchFlow {

    fun simpleFLow(): Flow<Int> = (1..5).asFlow()

    @Test
    fun testLaunch() = runBlocking {
        println("Started")
        simpleFLow().onEach { i -> println(i) }.collect()
        println("Done")
    }

    @Test
    fun testLaunchInFlow() = runBlocking<Unit> {
        simpleFLow().onEach { i -> println(i) }.launchIn(this) // return Job -> join, cancel..etc

        println("Done")
    }

    fun simpleFlowTwo(): Flow<Int> = flow {
        for (i in 1..5) {
            println("Emitting: $i")
            emit(i)
        }
    }

    // flow is cancellable emit value
    //  3 collected then after 4 is cancel
    @Test
    fun testCancellationChecks() = runBlocking<Unit> {
        simpleFlowTwo().collect { value ->
            if (value == 3) cancel()
            println(value)
        }

    }

    // flow operator do not do additional cancellation checks. don't suspend anywhere
    // note: print result 1..5 after cancel
    @Test
    fun testCancellationCheck2() = runBlocking<Unit> {
        (1..5).asFlow().collect { value ->
            if (value == 3) cancel()
            println(value)
        }
    }

    // Collected 3 and cancel occurs
    @Test
    fun testMakingFlowCancelable() = runBlocking<Unit> {
        (1..5).asFlow().cancellable().collect { value ->
            if (value == 3) cancel()
            println(value)
        }
    }

    // Collected 3 and cancel occurs
    @Test
    fun testMakingFlowCancelable2() = runBlocking<Unit> {
        (1..5).asFlow()
            .onEach {
                currentCoroutineContext().ensureActive()
            }
            .collect{ value ->
                if (value == 3) cancel()
                println(value)
            }
    }
}

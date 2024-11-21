package asyncFlow

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import kotlin.test.Test

class FLowContext {

    fun log(msg: String) = println("[${Thread.currentThread().name}] $msg]")

    fun numberFlow(): Flow<Int> = flow {
        log("Started number flow")
        for (i in 1..3) {
            emit(i)
        }
    }

    // change to flowOn retrieve from doing withContext
    fun numberFlowWrong(): Flow<Int> = flow {
        // The WRONG way to change context for CPU-consuming code in flow builder
        withContext(Dispatchers.Default) {
            for (i in 1..3) {
                delay(100L) // pretend we are computing it in CPU-consuming way
                log("Emitting $i")
                emit(i)
            }
        }
    }

    // flowOn withContext
    fun numberFlowWithContext(): Flow<Int> = flow {
        for (i in 1..3) {
            delay(100L) // pretend we are computing it in CPU-consuming way
            log("Emitting $i")
            emit(i)
        }
    }.flowOn(Dispatchers.Default) // RIGHT way to change context for CPU-consuming code in flow builder

    // this running Main Thread do not about execution context
    @Test
    fun testNumberFlow() = runBlocking<Unit> {
        numberFlow().collect { log("Collected $it") }
    }

    // Wrong And cause Exception, do not perform because run coroutine#1 -> Main Thread And context current Thread coroutine #1
    // not concurrently
    @Test
    fun testNumberWrongFlow() = runBlocking<Unit> {
        numberFlowWrong().collect { log("Collected $it") }
    }

    // correct Main Thread -> coroutine#1 And context run new another thread coroutine #2
    // concurrently occurs
    @Test
    fun testNumberWithContextFlow() = runBlocking<Unit> {
        numberFlowWithContext().collect { log("Collected $it") }
    }



}
package sharedMutableStateAndConcurrency

import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.newSingleThreadContext
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import org.junit.jupiter.api.Test
import java.util.concurrent.atomic.AtomicInteger
import kotlin.system.measureTimeMillis

class SharedMutableState {

    suspend fun massiveRun(action: suspend () -> Unit) {
        val n = 100
        var x = 1000

        val time = measureTimeMillis {
            coroutineScope {
                repeat(n){
                    launch{
                        repeat(x){
                            action()
                        }
                    }
                }
            }
        }

        println("Completed ${n * x} actions in $time ms")
    }

//    @Volatile
    var counter = AtomicInteger()

    @Test
    fun testSharedMutableState() = runBlocking {
        withContext(Dispatchers.Default) {
            massiveRun{
                counter.incrementAndGet()
            }
        }

        println("Counter = $counter")
    }

    @OptIn(DelicateCoroutinesApi::class, ExperimentalCoroutinesApi::class)
    val counterContext = newSingleThreadContext("CounterContext")
    var counterTwo = 0

    //a bit slowly
    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun testThreadConfinement() = runBlocking {
        withContext(Dispatchers.Default) {
            massiveRun{
                // confine each increment to a single-threaded context
                withContext(counterContext){
                    counterTwo++
                }
            }
        }
        println("Counter = $counterTwo")
    }

    //faster
    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun testThreadConfinementTwo() = runBlocking {
        // confine everything to a single-threaded context
        withContext(counterContext){
            massiveRun{
                counterTwo++
            }
        }
        println("Counter = $counterTwo")
    }

    val mutex = Mutex()
    @Test
    fun testMutex() = runBlocking {
        withContext(Dispatchers.Default){
            massiveRun{
                // protect each increment with lock
                mutex.withLock{
                    counterTwo++
                }
            }
        }
        println("Counter = $counterTwo")
    }
}



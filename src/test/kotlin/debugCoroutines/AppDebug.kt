package debugCoroutines

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.newSingleThreadContext
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import kotlin.coroutines.CoroutineContext
import kotlin.system.measureTimeMillis

fun log(msg: String) = println("[${Thread.currentThread().name}] $msg")

fun app() = runBlocking {
    val a = async<Int> {
        log("I'm compute a piece of the answer")
        10
    }

    val b = async<Int> {
        log("I'm compute another piece of the answer")
        20
    }
    println("Result $a + $b = ${a.await() + b.await()}")
}

@OptIn(DelicateCoroutinesApi::class, ExperimentalCoroutinesApi::class)
fun jumpingThread() {
    // use Closable Thread
    newSingleThreadContext("Ctx1").use { ctx1 ->
        newSingleThreadContext("Ctx2").use { ctx2 ->
            runBlocking(ctx1) {
                log("Started in ctx1")
                withContext(ctx2) {
                    log("Working in ctx2")
                }
                log("Back to ctx1")
            }
        }
    }
}

fun jobContext() = runBlocking {
    println("My job is ${coroutineContext[Job]}")
}

fun childrenCoroutine() = runBlocking {
    val request = launch {
        // override explicit Job Parent
        launch(Job()) {
            println("Job1: I run own in my job independently")
            delay(1000L)
            println("Job1: I am not affected by cancellation")
        }

        launch {
            delay(100L)
            println("Job2: I am a child request coroutine")
            delay(1000L)
            println("job2: i will not execute my parent cancellation")
        }
    }
    delay(500L)
    request.cancel()
    println("main: Who survived request cancellation")
    delay(1000L)
}

fun parentJoin() = runBlocking {
    val measure = measureTimeMillis {
        val request = launch {
            repeat(3) {
                    i ->
                launch{
                    delay((i + 1) * 200L)
                    println("Coroutine $i is done")
                }
            }
            println("Request: I'm done")
        }
        request.join()
        println("Now processing request is completed")
    }
    println("completed in $measure ms")
}

fun namingCoroutines() = runBlocking(CoroutineName("main")){
    log("Started main coroutine")
    val v1 = async<Int>(CoroutineName("v1coroutine")){
        delay(500L)
        log("compute v1")
        10
    }

    val v2 = async<Int>(CoroutineName("v2coroutine")){
        delay(1000L)
        log("compute v2")
        50
    }
    log("Result: $v1 + $v2 = ${v1.await() + v2.await()}")
}

fun combineContextElement() = runBlocking{
    launch(Dispatchers.Default + CoroutineName("test") ) {
        println("I'm working in Thread ${Thread.currentThread().name}")
    }
}

fun main() {
//    jumpingThread()
//    jobContext()
//    childrenCoroutine()
//    parentJoin()
//    namingCoroutines()
    combineContextElement()
}
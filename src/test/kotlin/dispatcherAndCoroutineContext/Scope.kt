package dispatcherAndCoroutineContext

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlin.system.measureTimeMillis


class Activity {

    val mainScope = CoroutineScope(Dispatchers.Default)

    fun onDestroy() = mainScope.cancel()

    fun doSomething() {
        repeat(10) { i ->
            mainScope.launch {
                delay((i + 1) * 200L)
                println("Coroutine $i is done")
            }
        }
    }
}

fun main() = runBlocking{
    val measure = measureTimeMillis{
        val activity = Activity()
        activity.doSomething()
        println("Launched coroutines")
        delay(500L)
        activity.onDestroy()
        println("Destroyed Activity")
        delay(1000L)
    }
    println("Took ${measure}ms")
}
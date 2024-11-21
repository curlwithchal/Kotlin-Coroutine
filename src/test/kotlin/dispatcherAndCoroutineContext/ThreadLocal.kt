package dispatcherAndCoroutineContext

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.asContextElement
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.yield

val threadLocal = ThreadLocal<String?>()

fun main() = runBlocking{
    threadLocal.set("main")
    println("Pre main, current thread: ${Thread.currentThread()} + thread local: ${threadLocal.get()}")
    val job = launch(Dispatchers.Default + threadLocal.asContextElement("launch")) {
        println("Launch start, current thread: ${Thread.currentThread()} + thread local: ${threadLocal.get()}")
        yield()
        println("After yield, current thread: ${Thread.currentThread()} + thread local: ${threadLocal.get()}")
    }

    job.join()
    println("Post Main, current thread: ${Thread.currentThread()} + thread local: ${threadLocal.get()}")
}
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.util.concurrent.CancellationException
import kotlin.test.Test


class CoroutineScope {


    suspend fun doRun() = coroutineScope {
        launch {
            delay(1000L)
            println("World!")
        }
        println("Hello")
    }

    @Test
    fun testDoRun() {
        fun run() = runBlocking {
            doRun()
        }
        run()
    }

    // concurrent execution DOG AND CAT
    suspend fun doAnimals() = runBlocking {
        launch {
            delay(2000L)
            println("DOG")
        }

        launch {
            delay(1000L)
            println("Cat")
        }
        println("Created")
    }

    @Test
    fun testDoAnimals() {
        // Sequentially execution DoAnimals() And DONE
        fun run() = runBlocking {
            doAnimals()
            println("Done")
        }
        run()
    }

    // An Explicit Job Waiting
    @Test
    fun runJob() {
        fun doRunJob() = runBlocking {
            println("Start")
            val job = launch {
                try {
                    delay(2500L)
                    println("Job")
                } catch (e: CancellationException) {
                    println("CoroutineCancellationException ${e.message}")
                } finally {
                    println("Clean Up")
                }
            }
//            delay(2000L)
//            job.cancel() // execution canceled Job
            job.join()
            println("Done")
        }
        doRunJob()
    }

    @Test
    fun testRandom(){
        fun runRandom() = runBlocking{
            // launch a lot of coroutines
            repeat(10_000) {
                launch{
                    delay(1000L)
                    println(it)
                }
            }
        }
        runRandom()
    }

}
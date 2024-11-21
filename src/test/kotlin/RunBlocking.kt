import kotlin.test.Test
import kotlinx.coroutines.*

class FirstThread {

    suspend fun doWorld(){
        delay(1000L)
        println("World!")
    }

    fun firstRunBlocking() = runBlocking  {
        println("Start Execution")
        launch{
            println("Coroutine 1 Start")
            delay(2000)
            println("Coroutine 1 End")
        }

        launch{
            println("Coroutine 2 Start")
            delay(1000)
            println("Coroutine 2 End")
        }
        println("End Execution")
    }


    @Test
    fun testRunBlocking() = firstRunBlocking()

    @Test
    fun secondRunBlocking() {
        fun runBlock() = runBlocking  {
            launch{
                doWorld()
            }
            println("Hello")
        }
        runBlock()
    }
}


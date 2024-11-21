import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.yield
import kotlin.test.Test

class Yield {


    fun doSomethingYield() = runBlocking {
        launch{
            repeat(5) { i ->
                println("Coroutine A $i")
                yield()
            }
        }

        launch{
            repeat(5) { i ->
                println("Coroutine B $i")
                yield()
            }
        }

    }

    @Test
    fun testYield() {
        doSomethingYield()
    }
}
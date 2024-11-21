package exceptionsHandling

import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.supervisorScope
import kotlinx.coroutines.yield
import kotlin.test.Test

class SupervisorJob {

    @Test
    fun testSupervisorJob() = runBlocking<Unit> {
        val supervisor = SupervisorJob()

        with(CoroutineScope(coroutineContext + supervisor)) {

            val firstChild = launch(CoroutineExceptionHandler { _, exc -> }) {
                println("First Child is Failing")
                throw AssertionError("The First child is Cancelled")
            }

            val secondChild = launch {
                firstChild.join()
                println("The first child is cancelled: ${firstChild.isCancelled}, but the second child is active")
                try {
                    delay(Long.MAX_VALUE)
                } finally {
                    println("The second child is cancelled because supervisor is was cancelled")
                }
            }

            firstChild.join()
            println("Cancelling supervisor")
            supervisor.cancel()
            secondChild.join()
        }
    }

    @Test
    fun testSupervisorScopeJob() = runBlocking<Unit>{
        try {
            supervisorScope{
                val child = launch{
                    try {
                        println("Child is Sleeping ...")
                        delay(Long.MAX_VALUE)
                    }finally {
                        println("First child is Cancelled")
                    }
                }
                yield()
                println("Throwing an exception from the supervisor Scope")
                throw AssertionError()
            }
        }catch (e: AssertionError){
            println("Caught AssertionError")
        }
    }

    @Test
    fun testSupervisorScopeJobHandleException() = runBlocking<Unit>{
        val handler = CoroutineExceptionHandler{_,exception ->
            println("CoroutineExceptionHandler got $exception")
        }
        supervisorScope{
            val childFirst = launch(handler) {
                println("Throw Child First Exception")
                throw AssertionError()
            }

            println("The Supervisor Scope Completing...")
        }
        println("Done Task")
    }

}
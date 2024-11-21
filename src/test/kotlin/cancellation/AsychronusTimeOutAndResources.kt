package cancellation

import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout

var acquired = 0

// Resource Leak
class Resource {
    init {
        acquired++ // acquired the resource
    }

    fun close() = acquired-- // Release resource
}


fun testResourceLeak() {
    runBlocking {
        repeat(10_000) {
            launch {
                val resource = withTimeout(60) {
                    delay(50)
                    Resource() //Acquire a resource and return it from withTimeout block
                }
                resource.close() // Release resource
            }
        }
    }

    // Outside Run Blocking all coroutines have completed
    //note -> not always zero because resource leak
    println(acquired) // Print the number resource of resource still required
}

fun testResourceNotLeak() {
    runBlocking {
        repeat(10_000) {
            launch {
                var resource: Resource? = null // Not Acquired yet
                try {
                    withTimeout(60) {
                        delay(50)
                        resource = Resource() // Store a resource to the variable if acquired
                    }
                    // We can do something else with the resource here
                }finally {
                    resource?.close() // Release resource
                }

            }
        }
    }

    // Outside Run Blocking all coroutines have completed
    //note -> not always zero because resource leak
    println(acquired) // Print the number resource of resource still required
}

fun main(){
//    testResourceLeak() // Print Not Always Zero Occurs Leak
    testResourceNotLeak() // Print Always Zero Because not Leak Occurs
}


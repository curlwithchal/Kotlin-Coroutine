package asyncFlow

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.zip
import kotlinx.coroutines.runBlocking
import kotlin.test.Test

class ComposingMultipleFlow {

    @Test
    fun testZipFLow() = runBlocking {
        val nums = (1..3).asFlow()
        val strs = flowOf("One", "Two", "Three")
        nums.zip(strs) { a, b -> "$a -> $b" }
            .collect { println(it) }
    }

    // get most recently value (3) instead zip
    // Execution Result times same strs delay 400ms++
    @Test
    fun testCombineFLow() = runBlocking {
        val nums = (1..3).asFlow().onEach { delay(300L) }
        val strs = flowOf("One", "Two", "Three").onEach { delay(400L) }
        val startTime = System.currentTimeMillis()
        nums.combine(strs) { a, b -> "$a -> $b" }.collect { value ->
            println("$value at ${System.currentTimeMillis() - startTime}.ms from start")
        }

    }
}
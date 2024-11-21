package asyncFlow

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.flatMapConcat
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flatMapMerge
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.runBlocking
import kotlin.test.Test

class Flattening {
    fun requestFlow(i: Int): Flow<String> = flow {
        emit("$i : First")
        delay(500L)
        emit("$i : Second")
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    //flatMapConcat
    fun testRequestFlow() = runBlocking {
        val startTime = System.currentTimeMillis()
        (1..3).asFlow()
            .onEach{delay(100L)}
            .flatMapConcat{requestFlow(it)}
            .collect { value ->
                println("$value at ${System.currentTimeMillis() - startTime} ms from start ${Thread.currentThread().name}")
            }

    }
    //flatMapMerge
    // concurrent occurs
    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun testRequestFlowMerge() = runBlocking{
        val startTime = System.currentTimeMillis()
        (1..3).asFlow()
            .onEach{delay(100L)}
            .flatMapMerge{requestFlow(it)}
            .collect { value ->
                println("$value at ${System.currentTimeMillis() - startTime} ms from start ${Thread.currentThread().name}")
            }
    }


    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun testRequestFlowMFlatMapLatest() = runBlocking{
        val startTime = System.currentTimeMillis()
        (1..3).asFlow()
            .flatMapLatest{requestFlow(it)}
            .collect { value ->
                println("$value at ${System.currentTimeMillis() - startTime} ms from start ${Thread.currentThread().name}")
            }
    }
}

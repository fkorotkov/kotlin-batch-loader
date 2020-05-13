package com.github.fkorotkov.loader

import kotlinx.coroutines.*
import kotlin.math.roundToInt
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.time.ExperimentalTime
import kotlin.time.measureTime

@ExperimentalCoroutinesApi
@ExperimentalTime
class BatchLoaderTest {
  @Test
  fun testThroughput() = runBlocking {
    val regularTime = measureTime { loadTest(TestLoader()) }
    println("Regular time ${regularTime.toLongMilliseconds()}ms")
    val batchedTime = measureTime { loadTest(BatchLoader(delegateLoader = TestLoader())) }
    println("Batched time ${batchedTime.toLongMilliseconds()}ms")
    val magnitude = (regularTime.toLongMilliseconds().toDouble() / batchedTime.toLongMilliseconds()).roundToInt()
    println("Batch loaded in ${batchedTime.toLongMilliseconds()}ms vs ${regularTime.toLongMilliseconds()}ms which is $magnitude times faster!")
    assertTrue(batchedTime < regularTime)
    assertTrue(magnitude >= 4, "Performance improvement is $magnitude")
  }

  private suspend fun loadTest(loader: Loader<Int, Int>) = coroutineScope {
    val requests = 3_000
    (1..requests).map { step ->
      val idToLoad = step % 300
      async {
        val result = loader.loadByIds(setOf(idToLoad))
        assertEquals(1, result.size)
        assertEquals(idToLoad, result[idToLoad])
      }
    }.awaitAll()
  }
}

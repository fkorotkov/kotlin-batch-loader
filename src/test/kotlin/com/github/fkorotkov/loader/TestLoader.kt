package com.github.fkorotkov.loader

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.random.Random

class TestLoader: Loader<Int, Int> {
  private val rand = Random(System.currentTimeMillis())

  override suspend fun loadByIds(ids: Set<Int>): Map<Int, Int> {
    // emulate API call in IO context
    return withContext(Dispatchers.IO) {
      // emulate response time from 10ms to 30ms
      Thread.sleep(rand.nextLong(10, 30))
      ids.map { it to it }.toMap()
    }
  }
}

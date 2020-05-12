package com.github.fkorotkov.loader

import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.selects.select
import java.util.*
import java.util.concurrent.Executors
import kotlin.collections.HashSet
import kotlin.coroutines.CoroutineContext
import kotlin.time.ExperimentalTime

@ExperimentalTime
@ExperimentalCoroutinesApi
class BatchLoader<ID, T>(
  poolSize: Int = 32,
  private val keyBatchSizeLimit: Int = 100,
  private val delegateLoader: Loader<ID, T>
) : Loader<ID, T>, CoroutineScope {
  override val coroutineContext: CoroutineContext = Executors.newFixedThreadPool(poolSize).asCoroutineDispatcher()

  private val requests = Channel<LoadRequest<ID, T>>(Channel.UNLIMITED)

  init {
    repeat(poolSize) {
      launch(coroutineContext) {
        while (true) {
          val requestsToProcess = LinkedList<LoadRequest<ID, T>>()
          val idsToLoad = HashSet<ID>()
          val request = requests.receive()
          requestsToProcess.add(request)
          idsToLoad.addAll(request.ids)
          while (idsToLoad.size < keyBatchSizeLimit) {
            // see if there are more requests to process
            val additionalRequest = select<LoadRequest<ID, T>?> {
              requests.onReceive { it }
              onTimeout(0) { null }
            } ?: break
            requestsToProcess.add(additionalRequest)
            idsToLoad.addAll(additionalRequest.ids)
          }
          try {
            val loadedObjects = delegateLoader.loadByIds(idsToLoad)
            requestsToProcess.forEach { it.fullFillFrom(loadedObjects) }
          } catch (e: Exception) {
            requestsToProcess.forEach { it.result.completeExceptionally(e) }
          }
        }
      }
    }
  }


  override suspend fun loadByIds(ids: Set<ID>): Map<ID, T> {
    val request = LoadRequest<ID, T>(ids)
    requests.send(request)
    return request.result.await()
  }
}

private data class LoadRequest<ID, T>(
  val ids: Set<ID>,
  val result: CompletableDeferred<Map<ID, T>> = CompletableDeferred<Map<ID, T>>()
) {
  fun fullFillFrom(loadedObjects: Map<ID, T>) {
    result.complete(
      loadedObjects.filterKeys { ids.contains(it) }
    )
  }
}

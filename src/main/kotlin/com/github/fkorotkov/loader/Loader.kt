package com.github.fkorotkov.loader

interface Loader<ID, T> {
  suspend fun loadByIds(ids: Set<ID>): Map<ID, T>
}

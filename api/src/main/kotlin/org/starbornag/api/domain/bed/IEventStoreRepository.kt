package org.starbornag.api.domain.bed

interface IEventStoreRepository<T> {
    fun fetch(streamName: String) : Sequence<T>
    fun append(streamName: String, events: Iterable<T>, expectedVersion: Long)
}
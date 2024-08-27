package org.starbornag.api.domain.bed

interface IEventStoreRepository<Input, Output> {
    fun fetch(streamName: String) : Sequence<Output>
    fun append(streamName: String, events: Iterable<Input>, expectedVersion: Long)
}
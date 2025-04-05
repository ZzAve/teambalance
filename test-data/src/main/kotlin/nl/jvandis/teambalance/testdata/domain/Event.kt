package nl.jvandis.teambalance.testdata.domain

import kotlinx.serialization.Serializable

@Serializable
data class Event<T>(
    val totalSize: Long,
    val totalPages: Int,
    val page: Int,
    val size: Int,
    val events: List<T>,
)

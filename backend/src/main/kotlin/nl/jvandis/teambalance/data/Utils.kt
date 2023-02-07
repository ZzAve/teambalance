package nl.jvandis.teambalance.data

import org.springframework.data.domain.Pageable

interface TeamBalanceEntityBuilder<E> {
    fun build(): E
}

fun <E> List<TeamBalanceEntityBuilder<E>>.build(): List<E> = map { it.build() }

fun limitOrDefault(pageable: Pageable): Int =
    if (pageable.isPaged) pageable.pageSize else DEFAULT_LIMIT

fun offsetOrDefault(pageable: Pageable): Long =
    if (pageable.isPaged) pageable.offset else NO_OFFSET

const val NO_ID = 0L

const val NO_OFFSET = 0L

const val DEFAULT_LIMIT = 10

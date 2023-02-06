package nl.jvandis.teambalance.data

import org.jooq.InsertValuesStep2
import org.jooq.InsertValuesStep3
import org.jooq.InsertValuesStep6
import org.jooq.Record
import org.jooq.TableField
import org.jooq.exception.DataAccessException
import org.springframework.data.domain.Pageable

interface TeamBalanceEntityBuilder<E> {
    fun build(): E
}

fun <E> List<TeamBalanceEntityBuilder<E>>.build(): List<E> = map { it.build() }

fun <T> Record.getField(
    tableField: TableField<out Record, T?>
): T? = this[tableField]

fun <T> Record.getFieldOrThrow(
    tableField: TableField<out Record, T?>
): T =
    getField(tableField)
        ?: throw DataAccessException("Property '$tableField' unexpectedly does not exist on record $this")

const val NO_ID = 0L

fun limitOrDefault(pageable: Pageable): Int =
    if (pageable.isPaged) pageable.pageSize else DEFAULT_LIMIT

fun offsetOrDefault(pageable: Pageable): Long =
    if (pageable.isPaged) pageable.offset else NO_OFFSET

const val NO_OFFSET = 0L

const val DEFAULT_LIMIT = 10

fun <U, R : Record, T1, T2> InsertValuesStep2<R, T1, T2>.valuesFrom(
    data: List<U>,
    t1Mapper: (t: U) -> T1,
    t2Mapper: (t: U) -> T2
): InsertValuesStep2<R, T1, T2> = apply {
    data.forEach { values(t1Mapper(it), t2Mapper(it)) }
}

fun <U, R : Record, T1, T2, T3> InsertValuesStep3<R, T1, T2, T3>.valuesFrom(
    data: List<U>,
    t1Mapper: (t: U) -> T1,
    t2Mapper: (t: U) -> T2,
    t3Mapper: (t: U) -> T3,
): InsertValuesStep3<R, T1, T2, T3> = apply {
    data.forEach { values(t1Mapper(it), t2Mapper(it), t3Mapper(it)) }
}

fun <U, R : Record, T1, T2, T3, T4, T5, T6> InsertValuesStep6<R, T1, T2, T3, T4, T5, T6>.valuesFrom(
    data: List<U>,
    t1Mapper: (t: U) -> T1,
    t2Mapper: (t: U) -> T2,
    t3Mapper: (t: U) -> T3,
    t4Mapper: (t: U) -> T4,
    t5Mapper: (t: U) -> T5,
    t6Mapper: (t: U) -> T6,
): InsertValuesStep6<R, T1, T2, T3, T4, T5, T6> = apply {
    data.forEach { values(t1Mapper(it), t2Mapper(it), t3Mapper(it), t4Mapper(it), t5Mapper(it), t6Mapper(it)) }
}

package nl.jvandis.teambalance.api.event

import org.jooq.Record
import org.jooq.Result
import java.util.function.Consumer

/**
 * RecordHandlerInterface tailored to JOOQ Record handling of TeamBalance records.
 *
 * It allows a user to define how to map a list of records into a list of entities of type {@code OUT}.
 * Implementations are urged to 'smartly' deal with the redundancy in SQL return, which occurs when using joins.
 *
 * To some extended inspired by Picnic's Yolo library
 *
 */
interface TeamBalanceRecordHandler<OUT> : Consumer<Record> {
    fun acceptOneOrNull(record: Record?): OUT? = record?.let { acceptOne(it) }
    fun acceptOne(record: Record): OUT = apply { accept(record) }.build().first()
    override fun accept(record: Record)
    fun build(): List<OUT>
}

fun <T> Record?.handleWith(recordHandler: TeamBalanceRecordHandler<T>): T? {
    return recordHandler.acceptOneOrNull(this)
}

fun <T> Result<Record>.handleWith(recordHandler: TeamBalanceRecordHandler<T>): List<T> =
    this.forEach(recordHandler).let { recordHandler.build() }

package nl.jvandis.teambalance.api.match

import org.jooq.Record
import org.jooq.RecordHandler

/**
 * // TODO: what am I?
 */
interface TeamBalanceRecordHandler<OUT> : RecordHandler<Record> {
    fun build(): List<OUT>
}

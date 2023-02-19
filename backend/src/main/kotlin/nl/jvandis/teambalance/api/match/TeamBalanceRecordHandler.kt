package nl.jvandis.teambalance.api.match

import org.jooq.Record
import java.util.function.Consumer

/**
 * // TODO: what am I?
 */
interface TeamBalanceRecordHandler<OUT> : Consumer<Record> {
    override fun accept(record: Record)
    fun build(): List<OUT>
}

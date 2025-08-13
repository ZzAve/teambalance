package nl.jvandis.teambalance.data

import nl.jvandis.teambalance.MultiTenantContext
import nl.jvandis.teambalance.Tenant
import org.jooq.DSLContext
import org.jooq.DeleteUsingStep
import org.jooq.Field
import org.jooq.InsertValuesStep2
import org.jooq.InsertValuesStep3
import org.jooq.InsertValuesStep4
import org.jooq.InsertValuesStep5
import org.jooq.InsertValuesStep6
import org.jooq.InsertValuesStep7
import org.jooq.Record
import org.jooq.Record1
import org.jooq.SelectField
import org.jooq.SelectFieldOrAsterisk
import org.jooq.SelectSelectStep
import org.jooq.Table
import org.jooq.UpdateSetFirstStep
import org.jooq.conf.MappedSchema
import org.jooq.conf.RenderMapping
import org.jooq.conf.Settings
import org.jooq.impl.DefaultDSLContext
import org.springframework.stereotype.Component
import java.util.regex.Pattern

/**
 * An extension on top of Jooq's DSLContext to allow for multitenancy by separate schema's.
 *
 * Rationale: Each interaction with a DSLContext (select, update, delete ...) is wrapped by a multi-tenant context
 * selector {@link MultiTenantContext}
 */
@Component
class MultiTenantDslContext(
    private val context: DSLContext,
) {
    private val contexts: MutableMap<Tenant, DSLContext> = mutableMapOf()

    private fun tenantContext(): DSLContext =
        contexts[MultiTenantContext.getCurrentTenant()]
            ?: createContext(MultiTenantContext.getCurrentTenant())

    private fun createContext(tenant: Tenant): DefaultDSLContext {
        // https://www.jooq.org/doc/latest/manual/sql-building/dsl-context/custom-settings/settings-render-mapping/#mapping-dev-to-my_book_world-with-jooq
        val settings: Settings =
            Settings().withRenderMapping(
                RenderMapping().withSchemata(
                    MappedSchema()
                        .withInputExpression(Pattern.compile("public"))
                        .withOutput(tenant.name.lowercase()),
                ),
            )
        val tenantContext =
            DefaultDSLContext(
                context.configuration().connectionProvider(),
                context.dialect(),
                settings,
            )
        contexts[tenant] = tenantContext
        return tenantContext
    }

    // From this point on all relevant context functions are defined and wrapped (this list only contains the
    // subset needed within teambalance, and is not intended to be complete

    fun select(fields: Collection<SelectFieldOrAsterisk>): SelectSelectStep<Record> = tenantContext().select(fields)

    fun select(vararg fields: SelectFieldOrAsterisk): SelectSelectStep<Record> = tenantContext().select(*fields)

    fun <T1> select(field1: SelectField<T1>): SelectSelectStep<Record1<T1>> = tenantContext().select(field1)

    fun <R : Record, T1, T2> insertInto(
        into: Table<R>,
        field1: Field<T1>,
        field2: Field<T2>,
    ): InsertValuesStep2<R, T1, T2> = tenantContext().insertInto(into, field1, field2)

    fun <R : Record, T1, T2, T3> insertInto(
        into: Table<R>,
        field1: Field<T1>,
        field2: Field<T2>,
        field3: Field<T3>,
    ): InsertValuesStep3<R, T1, T2, T3> = tenantContext().insertInto(into, field1, field2, field3)

    fun <R : Record, T1, T2, T3, T4> insertInto(
        into: Table<R>,
        field1: Field<T1>,
        field2: Field<T2>,
        field3: Field<T3>,
        field4: Field<T4>,
    ): InsertValuesStep4<R, T1, T2, T3, T4> = tenantContext().insertInto(into, field1, field2, field3, field4)

    fun <R : Record, T1, T2, T3, T4, T5> insertInto(
        into: Table<R>,
        field1: Field<T1>,
        field2: Field<T2>,
        field3: Field<T3>,
        field4: Field<T4>,
        field5: Field<T5>,
    ): InsertValuesStep5<R, T1, T2, T3, T4, T5> = tenantContext().insertInto(into, field1, field2, field3, field4, field5)

    fun <R : Record, T1, T2, T3, T4, T5, T6> insertInto(
        into: Table<R>,
        field1: Field<T1>,
        field2: Field<T2>,
        field3: Field<T3>,
        field4: Field<T4>,
        field5: Field<T5>,
        field6: Field<T6>,
    ): InsertValuesStep6<R, T1, T2, T3, T4, T5, T6> = tenantContext().insertInto(into, field1, field2, field3, field4, field5, field6)

    fun <R : Record, T1, T2, T3, T4, T5, T6, T7> insertInto(
        into: Table<R>,
        field1: Field<T1>,
        field2: Field<T2>,
        field3: Field<T3>,
        field4: Field<T4>,
        field5: Field<T5>,
        field6: Field<T6>,
        field7: Field<T7>,
    ): InsertValuesStep7<R, T1, T2, T3, T4, T5, T6, T7> =
        tenantContext().insertInto(into, field1, field2, field3, field4, field5, field6, field7)

    fun <R : Record> update(table: Table<R>): UpdateSetFirstStep<R> = tenantContext().update(table)

    fun <R : Record> deleteFrom(table: Table<R>): DeleteUsingStep<R> = tenantContext().deleteFrom(table)

    fun <R : Record> delete(table: Table<R>): DeleteUsingStep<R> = tenantContext().deleteFrom(table)
}

package nl.jvandis.teambalance.api.users

import nl.jvandis.jooq.support.valuesFrom
import nl.jvandis.teambalance.data.MultiTenantDslContext
import nl.jvandis.teambalance.data.NO_ID
import nl.jvandis.teambalance.data.jooq.schema.tables.records.UzerRecord
import nl.jvandis.teambalance.data.jooq.schema.tables.references.UZER
import org.jooq.exception.DataAccessException
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Repository

@Repository
class UserRepository(
    val context: MultiTenantDslContext
) {
    fun findByIdOrNull(userId: Long): User? =
        context.select()
            .from(UZER)
            .where(UZER.ID.eq(userId))
            .fetchOne()
            ?.into(UzerRecord::class.java)
            ?.into(User::class.java)

    // TODO: add sort
    fun findAll(sort: Sort = Sort.unsorted()): List<User> =
        context.select()
            .from(UZER)
            .orderBy(UZER.NAME.asc())
            .fetchInto(User::class.java)

    fun insertMany(users: List<User>): List<User> {
        if (users.isEmpty()) {
            return emptyList()
        }
        val usersResult = context.insertInto(
            UZER,
            UZER.ROLE,
            UZER.NAME,
            UZER.IS_ACTIVE,
            UZER.SHOW_FOR_MATCHES,
            UZER.SHOW_FOR_TRAININGS,
            UZER.JERSEY_NUMBER
        )
            .valuesFrom(
                users,
                { it.role },
                { it.name },
                { it.isActive },
                { it.showForMatches },
                { it.showForTrainings },
                { it.jerseyNumber }
            )
            .returningResult(UZER.fields().toList())
            .fetch()
            .into(User::class.java)

        return if (usersResult.size == users.size) {
            usersResult
        } else {
            throw DataAccessException("Could not insert users $users")
        }
    }

    fun deleteById(userId: Long) {
        if (userId == NO_ID) {
            throw IllegalStateException(
                "User with 'special' id $NO_ID can not be deleted. " +
                        "The special no id serves a special purpose in transforming items " +
                        "from records to entities and back"
            )
        }
        val execute = context.deleteFrom(UZER)
            .where(UZER.ID.eq(userId))
            .execute()
        if (execute != 1) {
            throw DataAccessException("Removed $execute users, expected to remove only 1")
        }
    }

    fun update(updatedUser: User): User {
        if (updatedUser.id == NO_ID) {
            throw IllegalStateException(
                "User with 'special' id $NO_ID can not be deleted. " +
                        "The special no id serves a special purpose in transforming items " +
                        "from records to entities and back"
            )
        }
        return context
            .update(UZER)
            .set(UZER.ROLE, updatedUser.role)
            .set(UZER.NAME, updatedUser.name)
            .set(UZER.IS_ACTIVE, updatedUser.isActive)
            .set(UZER.SHOW_FOR_MATCHES, updatedUser.showForMatches)
            .set(UZER.SHOW_FOR_TRAININGS, updatedUser.showForTrainings)
            .set(UZER.JERSEY_NUMBER, updatedUser.jerseyNumber)
            .where(UZER.ID.eq(updatedUser.id))
            .returning(
                UZER.ID,
                UZER.ROLE,
                UZER.NAME,
                UZER.IS_ACTIVE,
                UZER.SHOW_FOR_MATCHES,
                UZER.SHOW_FOR_TRAININGS,
                UZER.JERSEY_NUMBER
            )
            .fetchOne()
            ?.into(User::class.java)
            ?: throw DataAccessException("Could not update user with id ${updatedUser.id}")
    }

    fun insert(user: User): User = insertMany(listOf(user)).first()
}

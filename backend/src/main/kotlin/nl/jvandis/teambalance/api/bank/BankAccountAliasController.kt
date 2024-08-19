package nl.jvandis.teambalance.api.bank

import io.swagger.v3.oas.annotations.tags.Tag
import nl.jvandis.teambalance.TeamBalanceId
import nl.jvandis.teambalance.api.Admin
import nl.jvandis.teambalance.api.DataConstraintViolationException
import nl.jvandis.teambalance.api.InvalidAliasException
import nl.jvandis.teambalance.api.InvalidUserException
import nl.jvandis.teambalance.api.users.UserRepository
import org.jooq.exception.DataAccessException
import org.slf4j.LoggerFactory
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.http.HttpStatus
import org.springframework.http.HttpStatus.NO_CONTENT
import org.springframework.http.MediaType
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController

@RestController
@Admin
@Tag(name = "aliases")
@RequestMapping(path = ["/api/aliases"], produces = [MediaType.APPLICATION_JSON_VALUE])
class BankAccountAliasController(
    private val bankAccountAliasRepository: BankAccountAliasRepository,
    private val userRepository: UserRepository,
) {
    private val log = LoggerFactory.getLogger(javaClass)

    @GetMapping
    fun getAliases(): BankAccountAliasesResponse {
        log.debug("getAliases")

        // TODO add filter on userId

        val bankAccountAliases = bankAccountAliasRepository.findAll()
        return BankAccountAliases(bankAccountAliases = bankAccountAliases).expose()

    }

    //    @PreAuthorize("hasRole('admin')")
    @GetMapping("/{id}")
    fun getAlias(
        @PathVariable(value = "id") bankAccountAliasId: String,
    ): BankAccountAliasResponse {
        val bankAccountAliasTeamBalanceId = TeamBalanceId(bankAccountAliasId)
        log.debug("getUser $bankAccountAliasTeamBalanceId")

        return bankAccountAliasRepository.findByIdOrNull(bankAccountAliasTeamBalanceId)
            ?.expose()
            ?: throw InvalidAliasException(bankAccountAliasTeamBalanceId)
    }

    //    @PreAuthorize("hasRole('admin')")
    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping
    fun postUser(
        @RequestBody potentialBankAccountAlias: PotentialBankAccountAlias,
    ): BankAccountAliasResponse {
        log.debug("postBankAccountAlias {}", potentialBankAccountAlias)

        val bankAccountAlias = potentialBankAccountAlias.internalize()

        try {

            return bankAccountAliasRepository.insert(bankAccountAlias).expose()
        } catch (e: DataAccessException) {
            throw DataConstraintViolationException("Alias ${bankAccountAlias.alias} could not be inserted, as it already exists for a(nother) user.")
        }
    }

    //    @PreAuthorize("hasRole('admin')")
    @ResponseStatus(NO_CONTENT)
    @DeleteMapping("/{id}")
    fun updateUser(
        @PathVariable(value = "id") bankAccountAliasId: String,
    ) {
        log.debug("Deleting bankAccountAlias: $bankAccountAliasId")

        try {
            bankAccountAliasRepository.deleteById(TeamBalanceId(bankAccountAliasId))
        } catch (e: DataIntegrityViolationException) {
            throw DataConstraintViolationException("User $bankAccountAliasId could not be deleted.")
        }
    }

    private fun PotentialBankAccountAlias.internalize(): BankAccountAlias =
        TeamBalanceId(userId)
            .let { id -> userRepository.findByIdOrNull(id) ?: throw InvalidUserException(id) }
            .let { user ->
                BankAccountAlias(
                    alias = alias,
                    user = user
                )
            }
}

data class PotentialBankAccountAlias(
    val alias: String,
    val userId: String,
)

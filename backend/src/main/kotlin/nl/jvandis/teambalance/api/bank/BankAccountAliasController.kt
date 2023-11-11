package nl.jvandis.teambalance.api.bank

import io.swagger.v3.oas.annotations.tags.Tag
import nl.jvandis.teambalance.api.Admin
import nl.jvandis.teambalance.api.DataConstraintViolationException
import nl.jvandis.teambalance.api.InvalidUserException
import nl.jvandis.teambalance.api.users.UserRepository
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
@PreAuthorize("hasRole('admin')") // TODO: make me pretty in https://github.com/ZzAve/teambalance/issues/200
@Tag(name = "aliases")
@RequestMapping(path = ["/api/aliases"], produces = [MediaType.APPLICATION_JSON_VALUE])
class BankAccountAliasController(
    private val bankAccountAliasRepository: BankAccountAliasRepository,
    private val userRepository: UserRepository,
) {
    private val log = LoggerFactory.getLogger(javaClass)

    // TODO: make me pretty in https://github.com/ZzAve/teambalance/issues/200
//    @PreAuthorize("hasRole('admin')")
//    @PreAuthorize("permitAll()") // to make it public
    @GetMapping
    fun getAliases(): BankAccountAliases {
        log.debug("getAliases")

        // TODO add filter on userId
        return BankAccountAliases(bankAccountAliases = bankAccountAliasRepository.findAll())
    }

    //    @PreAuthorize("hasRole('admin')")
    @GetMapping("/{id}")
    fun getUser(
        @PathVariable(value = "id") bankAccountAliasId: Long,
    ): BankAccountAlias {
        log.debug("getUser $bankAccountAliasId")

        return bankAccountAliasRepository.findByIdOrNull(bankAccountAliasId) ?: throw InvalidUserException(
            bankAccountAliasId,
        )
    }

    //    @PreAuthorize("hasRole('admin')")
    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping
    fun postUser(
        @RequestBody potentialBankAccountAlias: PotentialBankAccountAlias,
    ) {
        log.debug("postBankAccountAlias $potentialBankAccountAlias")

        val bankAccountAlias = potentialBankAccountAlias.internalize()
        bankAccountAliasRepository.insert(bankAccountAlias)
    }

    //    @PreAuthorize("hasRole('admin')")
    @ResponseStatus(NO_CONTENT)
    @DeleteMapping("/{id}")
    fun updateUser(
        @PathVariable(value = "id") bankAccountAliasId: Long,
    ) {
        log.debug("deleting bankAccountAlias: $bankAccountAliasId")

        try {
            bankAccountAliasRepository.deleteById(bankAccountAliasId)
        } catch (e: DataIntegrityViolationException) {
            throw DataConstraintViolationException("User $bankAccountAliasId could not be deleted.")
        }
    }

    private fun PotentialBankAccountAlias.internalize(): BankAccountAlias {
        val user = userRepository.findByIdOrNull(this.userId) ?: throw InvalidUserException(userId)
        return BankAccountAlias(name, user)
    }
}

data class PotentialBankAccountAlias(
    val name: String,
    val userId: Long,
)

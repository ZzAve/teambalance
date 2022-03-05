package nl.jvandis.teambalance.api.bank

import io.swagger.v3.oas.annotations.tags.Tag
import nl.jvandis.teambalance.api.Admin
import nl.jvandis.teambalance.api.DataConstraintViolationException
import nl.jvandis.teambalance.api.InvalidUserException
import nl.jvandis.teambalance.api.SECRET_HEADER
import nl.jvandis.teambalance.api.SecretService
import nl.jvandis.teambalance.api.users.UserRepository
import org.slf4j.LoggerFactory
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.data.repository.findByIdOrNull
import org.springframework.http.HttpStatus
import org.springframework.http.HttpStatus.NO_CONTENT
import org.springframework.http.MediaType
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestHeader
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
    private val secretService: SecretService
) {

    private val log = LoggerFactory.getLogger(javaClass)

    @GetMapping
    @PreAuthorize("hasRole('admin')")
    fun getAliases(
        @RequestHeader(value = SECRET_HEADER, required = false) secret: String?
    ): BankAccountAliases {
        log.debug("getAliases")
        secretService.ensureSecret(secret)

        // TODO add filter on userId
        return BankAccountAliases(bankAccountAliases = bankAccountAliasRepository.findAll())
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('admin')")
    fun getUser(
        @PathVariable(value = "id") bankAccountAliasId: Long,
        @RequestHeader(value = SECRET_HEADER, required = false) secret: String?
    ): BankAccountAlias {
        log.debug("getUser $bankAccountAliasId")
        secretService.ensureSecret(secret)

        return bankAccountAliasRepository.findByIdOrNull(bankAccountAliasId) ?: throw InvalidUserException(
            bankAccountAliasId
        )
    }

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping
    @PreAuthorize("hasRole('admin')")
    fun postUser(
        @RequestBody potentialBankAccountAlias: PotentialBankAccountAlias,
        @RequestHeader(value = SECRET_HEADER, required = false) secret: String?
    ) {
        log.debug("postBankAccountAlias $potentialBankAccountAlias")
        secretService.ensureSecret(secret)

        val bankAccountAlias = potentialBankAccountAlias.internalize()
        bankAccountAliasRepository.save(bankAccountAlias)
    }

    @ResponseStatus(NO_CONTENT)
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('admin')")
    fun updateUser(
        @PathVariable(value = "id") bankAccountAliasId: Long,
        @RequestHeader(value = SECRET_HEADER, required = false) secret: String?
    ) {
        log.debug("deleting bankAccountAlias: $bankAccountAliasId")
        secretService.ensureSecret(secret)

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
    val userId: Long
)

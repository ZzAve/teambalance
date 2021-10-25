package nl.jvandis.teambalance.api.bank

import io.swagger.annotations.Api
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
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import java.time.LocalDate

@RestController
@Api(tags = ["transaction-exclusions"])
@RequestMapping(path = ["/api/transaction-exclusions"], produces = [MediaType.APPLICATION_JSON_VALUE])
class BankAccountTransactionExclusionController(
    private val transactionExclusionRepository: BankAccountTransactionExclusionRepository,
    private val secretService: SecretService
) {

    private val log = LoggerFactory.getLogger(javaClass)

    @GetMapping
    fun getTransactionExclusions(
        @RequestHeader(value = SECRET_HEADER, required = false) secret: String?
    ): TransactionExclusions {
        log.debug("getTransactionExclusion")
        secretService.ensureSecret(secret)

        return TransactionExclusions(transactionExclusionRepository.findAll())
    }

    @GetMapping("/{id}")
    fun getTransactionExclusion(
        @PathVariable(value = "id") transactionExclusionId: Long,
        @RequestHeader(value = SECRET_HEADER, required = false) secret: String?
    ): TransactionExclusion {
        log.debug("getTransactionExclusion $transactionExclusionId")
        secretService.ensureSecret(secret)

        return transactionExclusionRepository.findByIdOrNull(transactionExclusionId) ?: throw InvalidUserException(transactionExclusionId)
    }

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping
    fun postTransactionExclusion(
        @RequestBody potentialTransactionExclusion: PotentialTransactionExclusion,
        @RequestHeader(value = SECRET_HEADER, required = false) secret: String?
    ) {
        log.debug("postTransactionExclusion $potentialTransactionExclusion")
        secretService.ensureSecret(secret)

        val transactionExclusion = potentialTransactionExclusion.internalize()
        transactionExclusionRepository.save(transactionExclusion)
    }

    @ResponseStatus(NO_CONTENT)
    @DeleteMapping("/{id}")
    fun deleteTransactionExclusion(
        @PathVariable(value = "id") transactionExclusionId: Long,
        @RequestHeader(value = SECRET_HEADER, required = false) secret: String?
    ) {
        log.debug("deleting transactionExclusion: $transactionExclusionId")
        secretService.ensureSecret(secret)

        try {
            transactionExclusionRepository.deleteById(transactionExclusionId)
        } catch (e: DataIntegrityViolationException) {
            throw DataConstraintViolationException("TransactionExclusion $transactionExclusionId could not be deleted.")
        }
    }

    private fun PotentialTransactionExclusion.internalize(): TransactionExclusion {
        return TransactionExclusion(
            date = date,
            transactionId = transactionId,
            counterParty = counterParty,
            description = description
        );
    }
}

data class PotentialTransactionExclusion(
    val transactionId: Int?,
    val date: LocalDate?,
    val counterParty: String?,
    val description: String?
)

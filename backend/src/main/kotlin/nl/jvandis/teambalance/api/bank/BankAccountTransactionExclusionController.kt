package nl.jvandis.teambalance.api.bank

import io.swagger.v3.oas.annotations.tags.Tag
import nl.jvandis.teambalance.api.DataConstraintViolationException
import nl.jvandis.teambalance.api.InvalidUserException
import nl.jvandis.teambalance.filters.SecretService
import org.slf4j.LoggerFactory
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.http.HttpStatus
import org.springframework.http.HttpStatus.NO_CONTENT
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import java.time.LocalDate

@RestController
@Tag(name = "transaction-exclusions")
@RequestMapping(path = ["/api/transaction-exclusions"], produces = [MediaType.APPLICATION_JSON_VALUE])
class BankAccountTransactionExclusionController(
    private val transactionExclusionRepository: BankAccountTransactionExclusionRepository,
    private val secretService: SecretService,
) {
    private val log = LoggerFactory.getLogger(javaClass)

    @GetMapping
    fun getTransactionExclusions(): TransactionExclusions {
        log.debug("getTransactionExclusion")

        return TransactionExclusions(transactionExclusionRepository.findAll())
    }

    @GetMapping("/{id}")
    fun getTransactionExclusion(
        @PathVariable(value = "id") transactionExclusionId: Long,
    ): TransactionExclusion {
        log.debug("getTransactionExclusion $transactionExclusionId")

        return transactionExclusionRepository.findByIdOrNull(transactionExclusionId) ?: throw InvalidUserException(transactionExclusionId)
    }

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping
    fun postTransactionExclusion(
        @RequestBody potentialTransactionExclusion: PotentialTransactionExclusion,
    ) {
        log.debug("postTransactionExclusion $potentialTransactionExclusion")

        val transactionExclusion = potentialTransactionExclusion.internalize()
        transactionExclusionRepository.insert(transactionExclusion)
    }

    @ResponseStatus(NO_CONTENT)
    @DeleteMapping("/{id}")
    fun deleteTransactionExclusion(
        @PathVariable(value = "id") transactionExclusionId: Long,
    ) {
        log.debug("deleting transactionExclusion: $transactionExclusionId")

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
            description = description,
        )
    }
}

data class PotentialTransactionExclusion(
    val transactionId: Int?,
    val date: LocalDate?,
    val counterParty: String?,
    val description: String?,
)

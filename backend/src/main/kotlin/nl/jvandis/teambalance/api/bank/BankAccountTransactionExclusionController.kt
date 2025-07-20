package nl.jvandis.teambalance.api.bank

import io.swagger.v3.oas.annotations.tags.Tag
import nl.jvandis.teambalance.TeamBalanceId
import nl.jvandis.teambalance.api.DataConstraintViolationException
import nl.jvandis.teambalance.api.InvalidTransactionException
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
    fun getTransactionExclusions(): TransactionExclusionsResponse {
        log.debug("getTransactionExclusion")

        return TransactionExclusions(transactionExclusionRepository.findAll()).toResponse()
    }

    @GetMapping("/{id}")
    fun getTransactionExclusion(
        @PathVariable(value = "id") transactionExclusionId: String,
    ): TransactionExclusionResponse {
        val transactionTeamBalanceId = TeamBalanceId(transactionExclusionId)
        log.debug("getTransactionExclusion $transactionTeamBalanceId")

        return transactionExclusionRepository.findByIdOrNull(transactionTeamBalanceId)?.toResponse()
            ?: throw InvalidTransactionException(
                transactionTeamBalanceId,
            )
    }

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping
    fun postTransactionExclusion(
        @RequestBody potentialTransactionExclusion: PotentialTransactionExclusion,
    ): TransactionExclusionResponse {
        log.debug("postTransactionExclusion $potentialTransactionExclusion")
        check(
            potentialTransactionExclusion.transactionId != null ||
                potentialTransactionExclusion.date != null ||
                potentialTransactionExclusion.counterParty != null ||
                potentialTransactionExclusion.description != null,
        ) {
            "At least one field must be provided to create a transaction exclusion."
        }
        val transactionExclusion = transactionExclusionRepository.insert(potentialTransactionExclusion.internalize())
        return transactionExclusion.toResponse()
    }

    @ResponseStatus(NO_CONTENT)
    @DeleteMapping("/{id}")
    fun deleteTransactionExclusion(
        @PathVariable(value = "id") transactionExclusionId: TeamBalanceId,
    ) {
        log.debug("deleting transactionExclusion: $transactionExclusionId")

        try {
            transactionExclusionRepository.deleteById(transactionExclusionId)
        } catch (e: DataIntegrityViolationException) {
            throw DataConstraintViolationException("TransactionExclusion $transactionExclusionId could not be deleted.")
        }
    }

    private fun PotentialTransactionExclusion.internalize(): TransactionExclusion =
        TransactionExclusion(
            date = date,
            transactionId = transactionId,
            counterParty = counterParty,
            description = description,
        )
}

private fun TransactionExclusions.toResponse(): TransactionExclusionsResponse =
    TransactionExclusionsResponse(transactionExclusions.map { it.toResponse() })

private fun TransactionExclusion.toResponse(): TransactionExclusionResponse =
    TransactionExclusionResponse(
        id = teamBalanceId.value,
        date = date,
        transactionId = transactionId,
        counterParty = counterParty,
        description = description,
    )

data class TransactionExclusionsResponse(
    val transactionExclusions: List<TransactionExclusionResponse>,
)

data class TransactionExclusionResponse(
    val id: String,
    val date: LocalDate?,
    val transactionId: String?,
    val counterParty: String?,
    val description: String?,
)

data class PotentialTransactionExclusion(
    val transactionId: String?,
    val date: LocalDate?,
    val counterParty: String?,
    val description: String?,
)

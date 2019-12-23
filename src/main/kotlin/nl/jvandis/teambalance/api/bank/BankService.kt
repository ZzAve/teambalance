package nl.jvandis.teambalance.api.bank

import com.bunq.sdk.model.generated.`object`.Amount
import com.bunq.sdk.model.generated.endpoint.Payment
import org.springframework.stereotype.Service
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

private const val TEAM_BANK_ACCOUNT_ID = 1547165
private val FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSSSSS").withZone(ZoneId.of("UTC"))

/**
 * BankService resolves bankrelated questions
 *
 * Its main function is to retrieve information from the BunqRepository and make it available in domain objects.
 * Makes use of a BunqReposity to connect to the bunqAPI
 */
@Service
class BankService(
        private val bunqRepository: BunqRepository
) {
    fun getBalance(): String {
        val balance = bunqRepository.getMonetaryAccountBank(TEAM_BANK_ACCOUNT_ID).balance
        return parseAmount(balance)
    }


    fun getTransactions(limit: Int): Transactions =
            bunqRepository.getAllPayment(TEAM_BANK_ACCOUNT_ID, limit).let {
                Transactions(
                        transactions = it.map { payment -> payment.toDomain() },
                        limit = it.size
                )
            }.also {
                bunqRepository.updateContext()
            }

    private fun parseAmount(balance: Amount): String {
        val currencySymbol = if (balance.currency == "EUR") "€" else balance.currency
        return "$currencySymbol ${balance.value}"
    }

    private fun Payment.toDomain() = Transaction(
            id = id,
            amount = parseAmount(amount),
            counterParty = counterpartyAlias.displayName,
            date = created.toZonedDateTime()
    )

    private fun String.toZonedDateTime() = ZonedDateTime.parse(this, FORMATTER).withZoneSameInstant(ZoneId.of("Europe/Paris"))
}

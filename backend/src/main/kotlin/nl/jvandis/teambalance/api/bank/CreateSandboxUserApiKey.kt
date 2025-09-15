package nl.jvandis.teambalance.api.bank

import com.bunq.sdk.generated.Sdk
import com.bunq.sdk.generated.endpoint.CREATE_RequestInquiry_for_User_MonetaryAccount
import com.bunq.sdk.generated.endpoint.CREATE_SandboxUserPerson
import com.bunq.sdk.generated.model.Amount
import com.bunq.sdk.generated.model.CreateRequestInquiry
import com.bunq.sdk.generated.model.Pointer
import com.bunq.sdk.generated.model.SandboxUserPerson
import com.bunq.sdk.serialization
import community.flock.wirespec.kotlin.Wirespec
import nl.jvandis.teambalance.loggerFor
import java.net.URI
import java.net.URLEncoder
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse

private val log = loggerFor("nl.jvandis.teambalance.api.bank.CreateSandboxUserApiKey")

suspend fun requireSpendingMoney(
    userId: Long,
    monetaryAccountId: Long,
    sdk: Sdk,
) = try {
    when (
        val response =
            sdk.cREATE_RequestInquiry_for_User_MonetaryAccount(
                userID = userId,
                monetaryaccountID = monetaryAccountId,
                body =
                    CreateRequestInquiry(
                        amount_inquired =
                            Amount(
                                value = "123.45",
                                currency = "EUR",
                            ),
                        counterparty_alias =
                            Pointer(
                                type = "EMAIL",
                                value = "sugardaddy@bunq.com",
                                name = "Sugar Daddy",
                                service = null,
                            ),
                        description = "Asking sugar daddy for help",
                        allow_bunqme = false,
                    ),
            )
    ) {
        is CREATE_RequestInquiry_for_User_MonetaryAccount.Response200 -> {
            response.body
        }
        is CREATE_RequestInquiry_for_User_MonetaryAccount.Response400 -> error("Failed to create request inquiry to sugar daddy")
    }
} catch (e: Exception) {
    log.warn("Failed to create request inquiry to sugar daddy", e)
    null
}

fun createSandboxUserApiKey(baseUrl: String): String {
    require(baseUrl.isNotBlank()) { "Base URL cannot be blank" }
    require(baseUrl.startsWith("http")) { "Base URL must be a valid HTTP(S) URL" }

    val sandboxUserPersonResponse = createSandboxUserPerson(baseUrl, SandboxUserPerson)
    return when (sandboxUserPersonResponse) {
        is CREATE_SandboxUserPerson.Response200 -> {
            sandboxUserPersonResponse.body.ApiKey?.api_key
                ?: throw IllegalStateException("No sandbox API key found in response")
        }
        is CREATE_SandboxUserPerson.Response400 -> {
            throw IllegalArgumentException("Failed to create sandbox user: Bad request")
        }
    }
}

private fun createSandboxUserPerson(
    baseUrl: String,
    body: SandboxUserPerson,
): CREATE_SandboxUserPerson.Response<*> =
    CREATE_SandboxUserPerson
        .Request(body)
        .let { req ->
            val rawRequest = CREATE_SandboxUserPerson.toRequest(serialization, req)
            val wirespecRawRequest = rawRequest.copy(headers = rawRequest.headers)
            val rawResponse = send(baseUrl, wirespecRawRequest)
            return CREATE_SandboxUserPerson.fromResponse(serialization, rawResponse)
        }

/**
 * Ugly send method (copy pasted from sdk) to allow for generation of sandbox users. This doesn't require fancy setting
 * of headers, or unpacking of signed / bytearray responses.
 */
private fun send(
    baseUrl: String,
    req: Wirespec.RawRequest,
): Wirespec.RawResponse {
    val client =
        HttpClient
            .newBuilder()
            .build()

    val baseUri = URI(baseUrl + req.path.joinToString("/"))
    val uri =
        if (req.queries.isNotEmpty()) {
            val queryString =
                req.queries.entries.joinToString("&") { (key, value) ->
                    value.joinToString("&") { v -> "$key=${URLEncoder.encode(v, Charsets.UTF_8)}" }
                }
            URI("$baseUri?$queryString")
        } else {
            baseUri
        }

    val headers =
        req.headers
            .mapValues { (_, values) -> values.filter { it.isNotBlank() } }
            .flatMap { (key, values) -> values.map { key to it } }
            .flatMap { listOf(it.first, it.second) }
            .toTypedArray()
            .let { it + arrayOf("test", "value") }

    val requestBuilder =
        HttpRequest
            .newBuilder()
            .uri(uri)
            .method(
                req.method.uppercase(),
                req.body
                    ?.let {
                        HttpRequest.BodyPublishers
                            .ofString(it)
                    }
                    ?: HttpRequest.BodyPublishers
                        .noBody(),
            ).headers(*headers)

    // Send HTTP request
    val response =
        client.send(
            requestBuilder.build(),
            HttpResponse.BodyHandlers
                .ofString(),
        )

    // Construct Wirespec.RawResponse
    return Wirespec.RawResponse(
        statusCode = response.statusCode(),
        headers = response.headers().map(),
        body = response.body(),
    )
}

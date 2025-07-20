package nl.jvandis.teambalance.api.bank

import com.bunq.sdk.generated.endpoint.CREATE_SandboxUserPerson
import com.bunq.sdk.generated.model.SandboxUserPerson
import com.bunq.sdk.serialization
import community.flock.wirespec.kotlin.Wirespec

fun createSandboxUserApiKey(baseUrl: String): String {
    val sandboxUserPersonResponse = createSandboxUserPerson(baseUrl, SandboxUserPerson)
    return when (sandboxUserPersonResponse) {
        is CREATE_SandboxUserPerson.Response200 -> {
            sandboxUserPersonResponse.body.ApiKey?.api_key ?: error("No sandbox api key was found in the response ")
        }

        is CREATE_SandboxUserPerson.Response400 -> TODO()
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
        java.net.http.HttpClient
            .newBuilder()
            .build()

    val baseUri = java.net.URI(baseUrl + req.path.joinToString("/"))
    val uri =
        if (req.queries.isNotEmpty()) {
            val queryString =
                req.queries.entries.joinToString("&") { (key, value) ->
                    value.joinToString("&") { v -> "$key=${java.net.URLEncoder.encode(v, Charsets.UTF_8)}" }
                }
            java.net.URI("$baseUri?$queryString")
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
        java.net.http.HttpRequest
            .newBuilder()
            .uri(uri)
            .method(
                req.method.uppercase(),
                req.body
                    ?.let {
                        java.net.http.HttpRequest.BodyPublishers
                            .ofString(it)
                    }
                    ?: java.net.http.HttpRequest.BodyPublishers
                        .noBody(),
            ).headers(*headers)

    // Send HTTP request
    val response =
        client.send(
            requestBuilder.build(),
            java.net.http.HttpResponse.BodyHandlers
                .ofString(),
        )

    // Construct Wirespec.RawResponse
    return Wirespec.RawResponse(
        statusCode = response.statusCode(),
        headers = response.headers().map(),
        body = response.body(),
    )
}

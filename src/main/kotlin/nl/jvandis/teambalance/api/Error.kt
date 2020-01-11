package nl.jvandis.teambalance.api

import org.springframework.http.HttpStatus

data class Error(
        val status: HttpStatus,
        val reason: String
)

package nl.jvandis.teambalance

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinFeature
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import org.junit.jupiter.api.Test
import kotlin.test.assertTrue

class JacksonTest {
    @Test
    fun jackson() {
        val objectMapper =
            ObjectMapper()
                .registerKotlinModule {
                    enable(KotlinFeature.KotlinPropertyNameAsImplicitName)
                }

        val deviceServerCreate = Wrapper(Wrapper.Inner(1L))
        val res = objectMapper.writeValueAsString(deviceServerCreate)
        println("res: $res")
        assertTrue { res == """{"Id":{"id":1}}""" }

        val readValue = objectMapper.readValue(res, Wrapper::class.java)
        println("readValue: $readValue")
        assertTrue { readValue.Id.id == 1L }

        assert(readValue == deviceServerCreate)
    }
}

data class Wrapper(
    val Id: Inner,
) {
    data class Inner(
        val id: Long,
    )
}

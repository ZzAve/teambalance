package nl.jvandis.teambalance

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinFeature
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import org.junit.jupiter.api.Test

class JacksonTest {
    @Test
    fun jackson() {
        val objectMapper =
            ObjectMapper()
                .registerKotlinModule {
                    enable(KotlinFeature.KotlinPropertyNameAsImplicitName)
                }
//                .configure(MapperFeature.USE_STD_BEAN_NAMING, true)

        val deviceServerCreate = Wrapper(Wrapper.Inner(1L))
        val res = objectMapper.writeValueAsString(deviceServerCreate)
        println("res: $res")

        val readValue = objectMapper.readValue(res, Wrapper::class.java)
        println("readValue: $readValue")
    }
}

data class Wrapper(
//    @JsonProperty("Id")
    val Id: Inner,
) {
    data class Inner(
        val id: Long,
    )
}

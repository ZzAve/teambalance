// package nl.jvandis.teambalance
//
// import org.springframework.boot.test.autoconfigure.web.servlet.MockMvcBuilderCustomizer
// import org.springframework.stereotype.Component
// import org.springframework.test.web.servlet.RequestBuilder
// import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
// import org.springframework.test.web.servlet.setup.ConfigurableMockMvcBuilder
//
//
// /**
// * Whenever a mockmvc object is autoconfigured, this customizer should be picked up, and a default, usable, working, valid api key is set as
// * default authorization header to be applied on all tests if not overwritten.
// *
// */
// @Component
// class ApiKeyHeaderMockMvcBuilderCustomizer : MockMvcBuilderCustomizer {
//    override fun customize(builder: ConfigurableMockMvcBuilder<*>) {
//        // setting the parent (mergeable) default requestbuilder to ConfigurableMockMvcBuilder
//        // every specifically set value in the requestbuilder used in the test class will have priority over
//        // the values set in the parent.
//        // This means, the url will always be replaced, since "any" would not make any sense.
//        // In case of multi value properties (like headers), existing headers from our default builder they are either merged or appended,
//        // exactly what we want to achieve
//        // see https://docs.spring.io/spring-boot/docs/current/api/org/springframework/boot/test/autoconfigure/web/servlet/MockMvcBuilderCustomizer.html
//        // and https://docs.spring.io/spring/docs/current/javadoc-api/org/springframework/beans/Mergeable.html
//        val apiKeyRequestBuilder: RequestBuilder = MockMvcRequestBuilders.get("any")
//            .header("X-Secret", "dGVhbWJhbGFuY2U=")
//        builder.defaultRequest(apiKeyRequestBuilder)
//    }
// }

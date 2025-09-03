package com.divadvo.kotlinspring

import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.info.Info
import io.swagger.v3.oas.models.info.Contact
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class SwaggerConfig {

    @Bean
    fun openAPI(): OpenAPI {
        return OpenAPI()
            .info(
                Info()
                    .title("Spring File App API")
                    .description("REST API for file processing, log viewing, and system operations")
                    .version("1.0.0")
                    .contact(
                        Contact()
                            .name("Spring File App")
                            .url("https://github.com/divadvo/kotlin-spring")
                    )
            )
    }
}
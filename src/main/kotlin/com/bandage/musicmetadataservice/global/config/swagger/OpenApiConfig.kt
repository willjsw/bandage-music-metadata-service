package com.bandage.musicmetadataservice.global.config.swagger

import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.info.Contact
import io.swagger.v3.oas.models.info.Info
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class OpenApiConfig {
    @Bean
    fun bandageOpenApi(): OpenAPI =
        OpenAPI()
            .info(
                Info()
                    .title("Bandage-Music-MetaData API")
                    .description("Bandage Music-MetaData 서비스 API 명세서")
                    .version("v1.0.0")
                    .contact(
                        Contact()
                            .name("Sunwoo Jung")
                            .email("sunwoo1137@gmail.com")
                            .url("https://github.com/willjsw/bandage-music-metadata-service"),
                    ),
            )
}
